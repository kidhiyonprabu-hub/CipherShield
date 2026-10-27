package com.ciphershield.ui.components;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.List;

public final class FileDropHandler {

    public interface Listener {
        void onFilesDropped(List<File> files);
    }

    private FileDropHandler() {
    }

    public static void install(JComponent component, final Listener listener) {
        component.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.isEmpty()) {
                        return false;
                    }
                    listener.onFilesDropped(files);
                    return true;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(component,
                            "Could not accept the dropped file: " + e.getMessage(),
                            "File Drop Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        });
    }
}
