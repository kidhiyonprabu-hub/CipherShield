package com.ciphershield;

import com.ciphershield.service.HistoryService;
import com.ciphershield.ui.MainFrame;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Main application launcher for CipherShield with comprehensive startup diagnostics.
 */
public class CipherShieldApp {

    private static final File LOG_FILE = new File("startup.log");

    public static synchronized void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String line = "[" + timestamp + "] [" + Thread.currentThread().getName() + "] " + message;
        System.out.println(line);
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(line);
        } catch (Exception ignored) {}
    }

    public static synchronized void logError(String message, Throwable t) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        String line = "[" + timestamp + "] [ERROR] [" + Thread.currentThread().getName() + "] " + message;
        System.err.println(line);
        if (t != null) {
            t.printStackTrace(System.err);
        }
        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(line);
            if (t != null) {
                t.printStackTrace(pw);
            }
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        try {
            // Clear or start fresh log file
            try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, false))) {
                pw.println("=== CipherShield Diagnostic Startup Log ===");
            } catch (Exception ignored) {}

            log("Main method entered. Java version: " + System.getProperty("java.version") +
                    " (" + System.getProperty("java.vendor") + ")");
            log("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") +
                    " (" + System.getProperty("os.arch") + ")");
            log("Working directory: " + new File(".").getAbsolutePath());

            // Capture all uncaught exceptions across all threads
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    logError("UNCAUGHT EXCEPTION in thread " + t.getName() + " (" + t.getId() + ")", e);
                }
            });

            // Register shutdown hook to detect why/when JVM exits
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log("JVM ShutdownHook triggered! Listing active non-daemon threads:");
                    Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
                    for (Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
                        Thread thread = entry.getKey();
                        if (thread != Thread.currentThread()) {
                            log("Thread [" + thread.getName() + "] (isDaemon=" + thread.isDaemon() + ", state=" + thread.getState() + ")");
                            for (StackTraceElement ste : entry.getValue()) {
                                log("    at " + ste);
                            }
                        }
                    }
                    log("=== JVM Shutdown Complete ===");
                }
            }, "DiagnosticShutdownHook"));

            log("Setting UI antialiasing properties...");
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            log("Setting System Look and Feel...");
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                log("Look and Feel set to: " + UIManager.getLookAndFeel().getName());
            } catch (Throwable t) {
                logError("Failed to set Look and Feel, continuing with default", t);
            }

            log("Initializing HistoryService...");
            try {
                HistoryService.getInstance();
                log("HistoryService initialized successfully.");
            } catch (Throwable t) {
                logError("Failed to initialize HistoryService", t);
            }

            log("Scheduling UI creation on EDT via SwingUtilities.invokeLater...");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    log("EDT run() started.");
                    try {
                        log("Creating MainFrame instance...");
                        long t0 = System.currentTimeMillis();
                        MainFrame frame = new MainFrame();
                        long dt = System.currentTimeMillis() - t0;
                        log("MainFrame created in " + dt + " ms.");

                        log("Calling frame.setVisible(true)...");
                        frame.setVisible(true);

                        log("Frame visibility check: isVisible=" + frame.isVisible() +
                                ", isDisplayable=" + frame.isDisplayable() +
                                ", isShowing=" + frame.isShowing() +
                                ", bounds=" + frame.getBounds() +
                                ", extendedState=" + frame.getExtendedState());

                        // Bring to front
                        frame.toFront();
                        frame.requestFocus();
                        log("MainFrame successfully displayed and focused.");

                    } catch (Throwable t) {
                        logError("CRITICAL EXCEPTION during MainFrame creation/display on EDT", t);
                        JOptionPane.showMessageDialog(null,
                                "Fatal error launching CipherShield:\n" + t.toString(),
                                "Initialization Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            log("main() thread exiting normally (EDT running in background).");

        } catch (Throwable t) {
            logError("CRITICAL EXCEPTION in main() method", t);
        }
    }
}
