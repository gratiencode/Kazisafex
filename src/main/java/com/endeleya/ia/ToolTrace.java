package com.endeleya.ia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tools.MainUI;

/**
 * TRACE TEMPORAIRE DES EXECUTIONS D'OUTILS (A RETIRER).
 *
 * Objectif: verifier ce qui est reellement execute des que l'utilisateur tape
 * son prompt. Ecrit une ligne horodatee par evenement dans
 * {@code Media/proc/logs/tool-trace.log} et sur la sortie standard.
 *
 * L'ecriture disque est faite sur un thread daemon dedie: elle ne doit JAMAIS
 * bloquer le thread JavaFX qui envoie le message (sinon l'UI fige au clic).
 *
 * POUR RETIRER LA TRACE: supprimer cette classe et tous les appels
 * {@code ToolTrace.log(...)} (rechercher "ToolTrace").
 */
public final class ToolTrace {

    private static final String DIR = MainUI.cPath("/Media/proc/logs");
    private static final String FILE = DIR + File.separator + "tool-trace.log";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_CHARS = 8000;
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "tool-trace-writer");
        thread.setDaemon(true);
        return thread;
    });

    private ToolTrace() {
    }

    public static void log(String phase, String detail) {
        String line = "[" + LocalDateTime.now().format(FMT) + "] [" + safe(phase) + "] " + oneLine(detail);
        System.out.println("[TOOL-TRACE] " + line);
        WRITER.submit(() -> write(line));
    }

    private static void write(String line) {
        synchronized (ToolTrace.class) {
            try {
                File file = new File(FILE);
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
                    pw.println(line);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String oneLine(String value) {
        if (value == null) {
            return "";
        }
        String single = value.replace('\r', ' ').replace('\n', ' ');
        return single.length() > MAX_CHARS ? single.substring(0, MAX_CHARS) + "...[tronque]" : single;
    }
}
