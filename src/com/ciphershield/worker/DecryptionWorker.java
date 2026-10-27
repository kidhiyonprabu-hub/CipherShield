package com.ciphershield.worker;

import com.ciphershield.crypto.SecureFileHandler;
import com.ciphershield.service.HistoryService;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * Background SwingWorker for file decryption and authentication.
 *
 * CO3: Multithreading and Performance Enhancement.
 * CO5: Event-driven GUI programming.
 */
public class DecryptionWorker extends SwingWorker<Boolean, String> {

    public interface WorkerListener {
        void onProgressUpdate(int percentage, String message);
        void onDone(boolean success, Exception error);
    }

    private final File inputFile;
    private final File outputFile;
    private final char[] password;
    private final WorkerListener listener;
    private Exception error;

    public DecryptionWorker(File inputFile, File outputFile, char[] password, WorkerListener listener) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.password = password;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        try {
            SecureFileHandler.decryptFile(inputFile, outputFile, password, new SecureFileHandler.ProgressCallback() {
                @Override
                public void onProgress(int percentage, String status) {
                    setProgress(percentage);
                    publish(status);
                }

                @Override
                public boolean isCancelled() {
                    return DecryptionWorker.this.isCancelled();
                }
            });
            HistoryService.getInstance().recordOperation("FILE_DECRYPT", inputFile.getName(), true, "NONE", inputFile.length());
            return true;
        } catch (Exception e) {
            this.error = e;
            HistoryService.getInstance().recordOperation("FILE_DECRYPT", inputFile.getName(), false, e.getClass().getSimpleName(), inputFile.length());
            return false;
        }
    }

    @Override
    protected void process(List<String> chunks) {
        if (!chunks.isEmpty() && listener != null) {
            listener.onProgressUpdate(getProgress(), chunks.get(chunks.size() - 1));
        }
    }

    @Override
    protected void done() {
        if (listener != null) {
            boolean success = false;
            try {
                success = !isCancelled() && get();
            } catch (Exception e) {
                if (this.error == null) this.error = e;
            }
            listener.onDone(success, error);
        }
    }
}
