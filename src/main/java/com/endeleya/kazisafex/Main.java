package com.endeleya.kazisafex;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Point d'entrée du jar exécutable.
 *
 * Lance l'application normalement (pipeline graphique matériel). Si JavaFX
 * ne trouve aucun pipeline graphique ("Graphics Device initialization failed",
 * "no suitable pipeline found" - typique sous RDP, VM, machine sans GPU ou
 * librairies natives absentes), le processus se relance automatiquement avec
 * le renderer logiciel (-Dprism.order=sw). Fonctionne quel que soit l'OS et
 * le mode de lancement (java -jar, IDE, jpackage).
 */
public class Main {

    private static final String SW_FALLBACK_MARKER = "kazisafe.software.renderer";

    public static void main(String[] args) {
        // Mise a jour automatique: si une mise a jour a ete telechargee au
        // demarrage precedent, on la remplace dans le dossier d'installation
        // puis on se relance (le script s'occupe de copier le jar et de rouvrir
        // l'application). Rien ne doit tourner ici avant cette verification.
        try {
            tools.UpdateManager updater = new tools.UpdateManager(null);
            if (updater.applyPendingUpdateAtStartup()) {
                return;
            }
        } catch (Throwable t) {
            System.err.println("[Kazisafex] Verification de mise a jour en attente impossible : "
                    + (t.getMessage() == null ? t.toString() : t.getMessage()));
        }

        boolean alreadyInSoftwareMode = Boolean.getBoolean(SW_FALLBACK_MARKER)
                || System.getProperty("prism.order") != null;

        if (!alreadyInSoftwareMode) {
            try {
                Kazisafex.main(args);
                return;
            } catch (Throwable t) {
                if (isPipelineFailure(t)) {
                    System.err.println(
                            "[Kazisafex] Aucun pipeline graphique matériel disponible : " + rootMessage(t));
                    System.err.println("[Kazisafex] Relance avec le renderer logiciel (-Dprism.order=sw)");
                    if (relaunchWithSoftwareRenderer(args)) {
                        return;
                    }
                    System.err.println(
                            "[Kazisafex] Impossible de relancer, tentative en mode logiciel dans ce processus.");
                }
                throw t;
            }
        }
        Kazisafex.main(args);
    }

    private static boolean isPipelineFailure(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null && (msg.contains("no suitable pipeline")
                    || msg.contains("No toolkit found")
                    || msg.contains("Graphics Device initialization failed"))) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    private static String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) {
            cur = cur.getCause();
        }
        return cur.getMessage() == null ? t.toString() : cur.getMessage();
    }

    private static boolean relaunchWithSoftwareRenderer(String[] args) {
        try {
            String os = System.getProperty("os.name", "").toLowerCase();
            String javaBin = java.nio.file.Paths
                    .get(System.getProperty("java.home"), "bin", os.contains("win") ? "java.exe" : "java")
                    .toString();

            List<String> cmd = new ArrayList<>();
            cmd.add(javaBin);
            cmd.add("-Dprism.order=sw");
            cmd.add("-D" + SW_FALLBACK_MARKER + "=true");
            // Reprendre les options JVM du processus courant (--enable-preview,
            // -Xss32m, -XX:CompileCommand...) pour un comportement identique.
            for (String arg : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
                if (!arg.startsWith("-Dprism.order") && !arg.startsWith("-D" + SW_FALLBACK_MARKER)) {
                    cmd.add(arg);
                }
            }
            String classPath = System.getProperty("java.class.path");
            if (classPath == null || classPath.isEmpty()) {
                return false;
            }
            cmd.add("-cp");
            cmd.add(classPath);
            cmd.add(Main.class.getName());
            for (String a : args) {
                cmd.add(a);
            }

            new ProcessBuilder(cmd).inheritIO().start();
            return true;
        } catch (Exception e) {
            System.err.println("[Kazisafex] Erreur lors de la relance : " + e.getMessage());
            return false;
        }
    }
}
