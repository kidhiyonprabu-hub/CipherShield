package com.ciphershield.ui.panels;

import com.ciphershield.crypto.SecureFileHandler;
import com.ciphershield.ui.components.CardPanel;
import com.ciphershield.ui.components.FileDropHandler;
import com.ciphershield.ui.components.PageHeader;
import com.ciphershield.ui.components.StyledButton;
import com.ciphershield.ui.components.StyledPasswordField;
import com.ciphershield.ui.components.StyledTextField;
import com.ciphershield.ui.theme.IconFactory;
import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;
import com.ciphershield.worker.DecryptionWorker;
import com.ciphershield.worker.EncryptionWorker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.Arrays;

/**
 * File Encryption and Decryption Panel.
 * Uses AES-256-GCM authenticated encryption with versioned binary container (.senc).
 * Provides background processing (SwingWorker), progress reporting, and overwrite protection.
 *
 * CO3: Multithreading & Exception handling.
 * CO4: File I/O.
 * CO5: Swing Event-Driven GUI.
 */
public class FileCryptoPanel extends JPanel {

    private final StyledTextField sourcePathField;
    private final StyledTextField destDirField;
    private final StyledPasswordField passwordField;
    private final StyledPasswordField confirmPasswordField;
    private final JProgressBar progressBar;
    private final JLabel progressStatusLabel;
    private final StyledButton btnEncrypt;
    private final StyledButton btnDecrypt;
    private final StyledButton btnCancel;
    private final JLabel fileDetailsLabel;

    private File selectedFile;
    private File selectedOutputDir;
    private SwingWorker<?, ?> currentWorker;

    public FileCryptoPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        add(new PageHeader("Secure File Encryption & Decryption",
                "AES-256-GCM .senc container with atomic writes and tamper-evident tags."),
                BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // 1. File Selection Card
        CardPanel fileCard = new CardPanel(new BorderLayout(0, 10), 16);
        JLabel fileSecTitle = new JLabel("1. SELECT TARGET FILE & OUTPUT DIRECTORY");
        fileSecTitle.setFont(ThemeFonts.SECTION);
        fileSecTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        fileCard.add(fileSecTitle, BorderLayout.NORTH);

        JPanel fileForm = new JPanel(new GridBagLayout());
        fileForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Source file row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblSrc = new JLabel("Source File:");
        lblSrc.setFont(ThemeFonts.BODY_BOLD);
        lblSrc.setForeground(ThemeColors.TEXT_SECONDARY);
        fileForm.add(lblSrc, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        sourcePathField = new StyledTextField();
        sourcePathField.setEditable(false);
        fileForm.add(sourcePathField, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        StyledButton btnBrowseSrc = new StyledButton("Browse File...", StyledButton.ButtonType.OUTLINE);
        btnBrowseSrc.setIcon(IconFactory.createFileIcon(16, ThemeColors.TEXT_PRIMARY));
        btnBrowseSrc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseSourceFile();
            }
        });
        fileForm.add(btnBrowseSrc, gbc);
        FileDropHandler.install(sourcePathField, new FileDropHandler.Listener() {
            @Override
            public void onFilesDropped(List<File> files) {
                if (files.size() > 1) {
                    JOptionPane.showMessageDialog(FileCryptoPanel.this,
                            "File Security accepts one file at a time. The first dropped file was selected.",
                            "Multiple Files", JOptionPane.INFORMATION_MESSAGE);
                }
                selectSourceFile(files.get(0));
            }
        });
        FileDropHandler.install(this, new FileDropHandler.Listener() {
            @Override
            public void onFilesDropped(List<File> files) {
                if (files.size() > 1) {
                    JOptionPane.showMessageDialog(FileCryptoPanel.this,
                            "File Security accepts one file at a time. The first dropped file was selected.",
                            "Multiple Files", JOptionPane.INFORMATION_MESSAGE);
                }
                selectSourceFile(files.get(0));
            }
        });

        // Destination folder row
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblDst = new JLabel("Output Folder:");
        lblDst.setFont(ThemeFonts.BODY_BOLD);
        lblDst.setForeground(ThemeColors.TEXT_SECONDARY);
        fileForm.add(lblDst, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        destDirField = new StyledTextField();
        destDirField.setEditable(false);
        fileForm.add(destDirField, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        StyledButton btnBrowseDst = new StyledButton("Choose Dir...", StyledButton.ButtonType.OUTLINE);
        btnBrowseDst.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseOutputDir();
            }
        });
        fileForm.add(btnBrowseDst, gbc);

        fileCard.add(fileForm, BorderLayout.CENTER);

        fileDetailsLabel = new JLabel("No file selected.");
        fileDetailsLabel.setFont(ThemeFonts.SMALL);
        fileDetailsLabel.setForeground(ThemeColors.SECONDARY);
        fileCard.add(fileDetailsLabel, BorderLayout.SOUTH);

        center.add(fileCard);
        center.add(Box.createVerticalStrut(14));

        // 2. Credentials Card
        CardPanel passCard = new CardPanel(new BorderLayout(0, 10), 16);
        JLabel passSecTitle = new JLabel("2. SECURITY CREDENTIALS");
        passSecTitle.setFont(ThemeFonts.SECTION);
        passSecTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        passCard.add(passSecTitle, BorderLayout.NORTH);

        JPanel passGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        passGrid.setOpaque(false);

        JPanel p1 = new JPanel(new BorderLayout(0, 4));
        p1.setOpaque(false);
        JLabel l1 = new JLabel("Encryption / Decryption Password:");
        l1.setFont(ThemeFonts.SMALL);
        l1.setForeground(ThemeColors.TEXT_SECONDARY);
        passwordField = new StyledPasswordField();
        p1.add(l1, BorderLayout.NORTH);
        p1.add(passwordField, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout(0, 4));
        p2.setOpaque(false);
        JLabel l2 = new JLabel("Confirm Password (for Encryption):");
        l2.setFont(ThemeFonts.SMALL);
        l2.setForeground(ThemeColors.TEXT_SECONDARY);
        confirmPasswordField = new StyledPasswordField();
        p2.add(l2, BorderLayout.NORTH);
        p2.add(confirmPasswordField, BorderLayout.CENTER);

        passGrid.add(p1);
        passGrid.add(p2);
        passCard.add(passGrid, BorderLayout.CENTER);

        center.add(passCard);
        center.add(Box.createVerticalStrut(14));

        // 3. Progress & Status Card
        CardPanel progressCard = new CardPanel(new BorderLayout(0, 8), 16);
        JLabel prgTitle = new JLabel("3. OPERATION STATUS");
        prgTitle.setFont(ThemeFonts.SECTION);
        prgTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        progressCard.add(prgTitle, BorderLayout.NORTH);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(ThemeFonts.BUTTON);
        progressBar.setForeground(ThemeColors.PRIMARY);
        progressBar.setBackground(ThemeColors.INPUT_BG);
        progressBar.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER, 1));
        progressBar.setPreferredSize(new Dimension(600, 24));

        progressStatusLabel = new JLabel("Ready. Select a file and enter password to begin.");
        progressStatusLabel.setFont(ThemeFonts.BODY);
        progressStatusLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        JPanel prgBox = new JPanel(new BorderLayout(0, 6));
        prgBox.setOpaque(false);
        prgBox.add(progressBar, BorderLayout.NORTH);
        prgBox.add(progressStatusLabel, BorderLayout.SOUTH);
        progressCard.add(prgBox, BorderLayout.CENTER);

        center.add(progressCard);

        add(center, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomBar.setOpaque(false);

        btnEncrypt = new StyledButton("Encrypt File -> .senc", StyledButton.ButtonType.PRIMARY);
        btnEncrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startFileEncryption();
            }
        });

        btnDecrypt = new StyledButton("Decrypt .senc File", StyledButton.ButtonType.SECONDARY);
        btnDecrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startFileDecryption();
            }
        });

        btnCancel = new StyledButton("Cancel", StyledButton.ButtonType.DANGER);
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentWorker != null && !currentWorker.isDone()) {
                    currentWorker.cancel(true);
                    progressStatusLabel.setText("Cancelling operation...");
                    progressStatusLabel.setForeground(ThemeColors.WARNING);
                }
            }
        });

        bottomBar.add(btnEncrypt);
        bottomBar.add(btnDecrypt);
        bottomBar.add(btnCancel);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private void chooseSourceFile() {
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            selectSourceFile(chooser.getSelectedFile());
        }
    }

    private void selectSourceFile(File file) {
        if (file == null || !file.isFile() || !file.canRead()) {
            JOptionPane.showMessageDialog(this, "Select a readable regular file.",
                    "Invalid File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectedFile = file;
        sourcePathField.setText(selectedFile.getAbsolutePath());
        if (selectedOutputDir == null) {
            selectedOutputDir = selectedFile.getParentFile();
            destDirField.setText(selectedOutputDir.getAbsolutePath());
        }
        long size = selectedFile.length();
        String sizeText;
        if (size < 1024) sizeText = size + " Bytes";
        else if (size < 1024 * 1024) sizeText = String.format("%.2f KB", size / 1024.0);
        else sizeText = String.format("%.2f MB", size / (1024.0 * 1024.0));
        fileDetailsLabel.setText("Target File: " + selectedFile.getName() + " (" + sizeText + ")");
    }

    private void chooseOutputDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            selectedOutputDir = chooser.getSelectedFile();
            destDirField.setText(selectedOutputDir.getAbsolutePath());
        }
    }

    private void setWorkingState(boolean working) {
        btnEncrypt.setEnabled(!working);
        btnDecrypt.setEnabled(!working);
        btnCancel.setEnabled(working);
    }

    private void startFileEncryption() {
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, "Please select an existing source file to encrypt.", "Missing File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedOutputDir == null || !selectedOutputDir.exists()) {
            JOptionPane.showMessageDialog(this, "Please select a valid output directory.", "Missing Directory", JOptionPane.WARNING_MESSAGE);
            return;
        }
        char[] pass = passwordField.getPassword();
        char[] confirm = confirmPasswordField.getPassword();

        if (pass.length == 0) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Empty Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Arrays.equals(pass, confirm)) {
            Arrays.fill(pass, '\0');
            Arrays.fill(confirm, '\0');
            passwordField.clearPassword();
            confirmPasswordField.clearPassword();
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Password Mismatch", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File targetFile = SecureFileHandler.getAvailableOutputFile(selectedOutputDir,
                selectedFile.getName() + SecureFileHandler.ENCRYPTED_EXTENSION);

        setWorkingState(true);
        progressBar.setValue(0);
        progressStatusLabel.setText("Starting encryption...");
        progressStatusLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        final char[] passCopy = Arrays.copyOf(pass, pass.length);
        Arrays.fill(pass, '\0');
        Arrays.fill(confirm, '\0');
        passwordField.clearPassword();
        confirmPasswordField.clearPassword();

        EncryptionWorker worker = new EncryptionWorker(selectedFile, targetFile, passCopy, new EncryptionWorker.WorkerListener() {
            @Override
            public void onProgressUpdate(int percentage, String message) {
                progressBar.setValue(percentage);
                progressStatusLabel.setText(message);
            }

            @Override
            public void onDone(boolean success, Exception error) {
                Arrays.fill(passCopy, '\0');
                passwordField.clearPassword();
                confirmPasswordField.clearPassword();
                setWorkingState(false);
                if (success) {
                    progressBar.setValue(100);
                    progressStatusLabel.setText("File successfully encrypted: " + targetFile.getName());
                    progressStatusLabel.setForeground(ThemeColors.SUCCESS);
                    JOptionPane.showMessageDialog(FileCryptoPanel.this,
                            "File encrypted successfully!\nSaved to: " + targetFile.getAbsolutePath(),
                            "Encryption Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String msg = (error != null) ? error.getMessage() : "Encryption failed.";
                    progressStatusLabel.setText("Error: " + msg);
                    progressStatusLabel.setForeground(ThemeColors.ERROR);
                    JOptionPane.showMessageDialog(FileCryptoPanel.this,
                            "Encryption failed: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        currentWorker = worker;
        worker.execute();
    }

    private void startFileDecryption() {
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, "Please select an encrypted (.senc) file.", "Missing File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedOutputDir == null || !selectedOutputDir.exists()) {
            JOptionPane.showMessageDialog(this, "Please select an output directory.", "Missing Directory", JOptionPane.WARNING_MESSAGE);
            return;
        }
        char[] pass = passwordField.getPassword();
        if (pass.length == 0) {
            JOptionPane.showMessageDialog(this, "Please enter the decryption password.", "Empty Password", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String origName = selectedFile.getName();
        String outputName;
        if (origName.endsWith(SecureFileHandler.ENCRYPTED_EXTENSION)) {
            outputName = origName.substring(0, origName.length() - SecureFileHandler.ENCRYPTED_EXTENSION.length());
        } else {
            outputName = origName + ".decrypted";
        }

        File targetFile = SecureFileHandler.getAvailableOutputFile(selectedOutputDir, outputName);

        setWorkingState(true);
        progressBar.setValue(0);
        progressStatusLabel.setText("Verifying header and decrypting...");
        progressStatusLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        final char[] passCopy = Arrays.copyOf(pass, pass.length);
        Arrays.fill(pass, '\0');
        passwordField.clearPassword();
        confirmPasswordField.clearPassword();

        DecryptionWorker worker = new DecryptionWorker(selectedFile, targetFile, passCopy, new DecryptionWorker.WorkerListener() {
            @Override
            public void onProgressUpdate(int percentage, String message) {
                progressBar.setValue(percentage);
                progressStatusLabel.setText(message);
            }

            @Override
            public void onDone(boolean success, Exception error) {
                Arrays.fill(passCopy, '\0');
                passwordField.clearPassword();
                confirmPasswordField.clearPassword();
                setWorkingState(false);
                if (success) {
                    progressBar.setValue(100);
                    progressStatusLabel.setText("File authenticated and decrypted: " + targetFile.getName());
                    progressStatusLabel.setForeground(ThemeColors.SUCCESS);
                    JOptionPane.showMessageDialog(FileCryptoPanel.this,
                            "File decrypted successfully!\nSaved to: " + targetFile.getAbsolutePath(),
                            "Decryption Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    String msg = (error != null) ? error.getMessage() : "Decryption failed.";
                    progressStatusLabel.setText("Error: " + msg);
                    progressStatusLabel.setForeground(ThemeColors.ERROR);
                    JOptionPane.showMessageDialog(FileCryptoPanel.this,
                            "Decryption failed: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        currentWorker = worker;
        worker.execute();
    }
}
