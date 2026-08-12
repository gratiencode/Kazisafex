package com.endeleya.ia;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Bootstrap de Redis pour la memoire de Gratien.
 * <p>
 * Redis sert de memoire persistante pour Gratien (contexte par
 * entreprise/utilisateur). A l'INSTALLATION est tentee uniquement au premier
 * lancement (cle de preference {@code redis.installed}) ; le DEMARRAGE d'un
 * redis-server deja installe est tente a chaque lancement si Redis est
 * arrete. Tout echec est non fatal : {@link RedisMemoryStore} bascule sur une
 * memoire locale.
 * </p>
 *
 * <p>Elevation de privileges sur Linux via {@code pkexec} (dialogue polkit
 * graphique) ou {@code sudo} si sans mot de passe ; sur macOS via Homebrew.
 * Aucun mot de passe n'est lu par l'application.</p>
 */
public final class RedisBootstrap {

    private static final Logger LOGGER = Logger.getLogger(RedisBootstrap.class.getName());
    private static final String REDIS_INSTALLED_KEY = "redis.installed";
    private static final int PROCESS_TIMEOUT_SECONDS = 180;

    private RedisBootstrap() {
    }

    /**
     * Verifie Redis et, si besoin, demarre ou installe (premier lancement).
     * A appeler sur un thread de fond (daemon) au demarrage de l'application.
     */
    public static void ensureRedis() {
        RedisMemoryStore probe = new RedisMemoryStore();
        if (probe.isRedisAvailable()) {
            return;
        }
        LOGGER.info("Redis indisponible pour Gratien - tentative de demarrage/installation.");

        Preferences pref = Preferences.userNodeForPackage(RedisBootstrap.class);
        boolean alreadyInstalled = pref.getBoolean(REDIS_INSTALLED_KEY, false);

        // 1. Demarrer un redis-server deja installe mais arrete (a chaque lancement).
        if (startRedisIfInstalled()) {
            markInstalled(pref);
            finish();
            return;
        }

        if (alreadyInstalled) {
            LOGGER.warning("redis-server present mais impossible a demarrer - "
                    + "Gratien utilise la memoire locale (Redis manquant/arrete).");
            finish();
            return;
        }

        // 2. Premier lancement uniquement : tenter l'installation.
        LOGGER.info("Premier lancement - tentative d'installation de Redis.");
        if (installRedis()) {
            markInstalled(pref);
            if (startRedisIfInstalled()) {
                LOGGER.info("Redis installe et demarre avec succes.");
            } else {
                LOGGER.warning("Redis installe mais non demarre - "
                        + "Gratien utilise la memoire locale jusqu'au prochain lancement.");
            }
        }
        finish();
    }

    private static void finish() {
        try {
            AiAgents.getInstance().recheckRedisMemory();
        } catch (Throwable ex) {
            // Ne jamais faire echouer le bootstrap a cause du recheck.
            LOGGER.log(Level.WARNING, "Recheck Redis apres bootstrap impossible", ex);
        }
    }

    private static void markInstalled(Preferences pref) {
        pref.putBoolean(REDIS_INSTALLED_KEY, true);
    }

    private static boolean startRedisIfInstalled() {
        String binary = findBinary("redis-server");
        if (binary == null) {
            return false;
        }
        if (pingRedis()) {
            return true;
        }
        runCommand(List.of(binary, "--daemonize", "yes",
                "--bind", "127.0.0.1", "--port", "6379"));
        return pingRedis();
    }

    private static boolean installRedis() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("linux")) {
                if (commandExists("apt-get")) {
                    return runElevated(Arrays.asList("apt-get", "install", "-y", "redis-server"))
                            || runElevated(Arrays.asList("apt-get", "install", "-y", "redis"));
                }
                if (commandExists("dnf")) {
                    return runElevated(Arrays.asList("dnf", "install", "-y", "redis"));
                }
                if (commandExists("yum")) {
                    return runElevated(Arrays.asList("yum", "install", "-y", "redis"));
                }
                if (commandExists("zypper")) {
                    return runElevated(Arrays.asList("zypper", "install", "-y", "redis"));
                }
                if (commandExists("pacman")) {
                    return runElevated(Arrays.asList("pacman", "-S", "--noconfirm", "redis"));
                }
                LOGGER.warning("Gestionnaire de paquets non reconnu sur Linux. "
                        + "Installez Redis manuellement (sudo apt-get install redis-server), "
                        + "puis relancez Kazisafex.");
                return false;
            }
            if (os.contains("mac") || os.contains("darwin")) {
                if (commandExists("brew")) {
                    return runCommand(List.of("brew", "install", "redis")) == 0;
                }
                LOGGER.warning("Homebrew introuvable. Installez Redis manuellement "
                        + "(brew install redis), puis relancez Kazisafex.");
                return false;
            }
            if (os.contains("win")) {
                LOGGER.warning("Installation automatique de Redis non supportee sur Windows. "
                        + "Installez Redis manuellement, puis relancez Kazisafex.");
                return false;
            }
            LOGGER.warning("OS non reconnu - installez Redis manuellement, puis relancez Kazisafex.");
            return false;
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Echec de l'installation de Redis", ex);
            return false;
        }
    }

    /**
     * Execute une commande avec elevation : pkexec (dialogue polkit graphique)
     * puis sudo si sans mot de passe. Aucun mot de passe n'est sollicite par
     * l'application elle-meme.
     */
    private static boolean runElevated(List<String> command) {
        if (commandExists("pkexec")) {
            return runCommand(prepend("pkexec", command)) == 0;
        }
        if (isPasswordlessSudo()) {
            return runCommand(prepend("sudo", command)) == 0;
        }
        LOGGER.warning("Elevation impossible (ni pkexec ni sudo sans mot de passe). "
                + "Installez Redis manuellement, puis relancez Kazisafex.");
        return false;
    }

    private static int runCommand(List<String> command) {
        try {
            LOGGER.info("Exec: " + String.join(" ", command));
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                LOGGER.warning("Commande expiree apres " + PROCESS_TIMEOUT_SECONDS
                        + "s: " + String.join(" ", command));
                return -1;
            }
            int exit = process.exitValue();
            LOGGER.info("Exit " + exit + ": " + String.join(" ", command));
            return exit;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Impossible d'executer: " + command, ex);
            return -1;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    private static List<String> prepend(String prefix, List<String> command) {
        List<String> result = new ArrayList<>(command.size() + 1);
        result.add(prefix);
        result.addAll(command);
        return result;
    }

    private static boolean pingRedis() {
        try {
            return new RedisMemoryStore().isRedisAvailable();
        } catch (Throwable ex) {
            return false;
        }
    }

    private static boolean commandExists(String name) {
        try {
            return which(name) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean isPasswordlessSudo() {
        try {
            Process process = new ProcessBuilder("sudo", "-n", "true").start();
            return process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (IOException | InterruptedException ex) {
            return false;
        }
    }

    private static String findBinary(String name) {
        for (String path : List.of(
                "/usr/bin/" + name,
                "/usr/local/bin/" + name,
                "/opt/homebrew/bin/" + name,
                "/bin/" + name)) {
            if (new File(path).canExecute()) {
                return path;
            }
        }
        // Fallback: search PATH entries for an executable with this name.
        String fromPath = which(name);
        if (fromPath != null) {
            return fromPath;
        }
        return null;
    }

    private static String which(String name) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isEmpty()) {
            return null;
        }
        for (String dir : pathEnv.split(File.pathSeparator)) {
            try {
                File candidate = new File(dir, name);
                if (candidate.canExecute()) {
                    return candidate.getAbsolutePath();
                }
                // On Unix, allow common extensions/variants
                File candidateSh = new File(dir, name + ".sh");
                if (candidateSh.canExecute()) {
                    return candidateSh.getAbsolutePath();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
