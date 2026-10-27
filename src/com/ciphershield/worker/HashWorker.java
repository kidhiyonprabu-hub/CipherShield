package com.ciphershield.worker;

import com.ciphershield.crypto.HashEngine;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * SwingWorker for calculating SHA-256 fingerprints in background without freezing UI.
 */
public class HashWorker extends SwingWorker<String, Integer> {

    public interface HashListener {
        void onProgress(int percentage);
        void onDone(String hash, Exception error);
    }

    private final File file;
    private final HashListener listener;
    private Exception error;

    public HashWorker(File file, HashListener listener) {
        this.file = file;
        this.listener = listener;
    }

    @Override
    protected String doInBackground() throws Exception {
        try {
            return HashEngine.calculateFileSHA256(file, new HashEngine.HashProgressCallback() {
                @Override
                public void onProgress(int percentage) {
                    setProgress(percentage);
                    publish(percentage);
                }

                @Override
                public boolean isCancelled() {
                    return HashWorker.this.isCancelled();
                }
            });
        } catch (Exception e) {
            this.error = e;
            return null;
        }
    }

    @Override
    protected void process(List<Integer> chunks) {
        if (!chunks.isEmpty() && listener != null) {
            listener.onProgress(chunks.get(chunks.size() - 1));
        }
    }

    @Override
    protected void done() {
        if (listener != null) {
            String hash = null;
            try {
                if (!isCancelled()) {
                    hash = get();
                }
            } catch (Exception e) {
                if (this.error == null) this.error = e;
            }
            listener.onDone(hash, error);
        }
    }
}
