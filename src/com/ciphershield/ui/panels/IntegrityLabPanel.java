package com.ciphershield.ui.panels;

import com.ciphershield.crypto.HashEngine;
import com.ciphershield.service.HistoryService;
import com.ciphershield.ui.components.CardPanel;
import com.ciphershield.ui.components.PageHeader;
import com.ciphershield.ui.components.StyledButton;
import com.ciphershield.ui.components.StyledTextField;
import com.ciphershield.ui.theme.IconFactory;
import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;
import com.ciphershield.worker.HashWorker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Integrity Lab: SHA-256 cryptographic checksum calculation and verification.
 * Emphasizes distinction between passive hashing and authenticated encryption (AES-GCM).
 *
 * CO4: File I/O and Hash Generation.
 */
public class IntegrityLabPanel extends JPanel {

    private final StyledTextField filePathField;
    private final StyledTextField generatedHashField;
    private final StyledTextField expectedHashField;
    private final JLabel matchResultBadge;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private File selectedFile;

    public IntegrityLabPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        add(new PageHeader("Integrity Lab – SHA-256 Checksum Engine",
                "Compute hashes and compare file fingerprints against trusted digests."),
                BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        // 1. File Selection Card
        CardPanel fileCard = new CardPanel(new BorderLayout(0, 10), 16);
        JLabel fileSec = new JLabel("1. SELECT FILE TO AUDIT");
        fileSec.setFont(ThemeFonts.SECTION);
        fileSec.setForeground(ThemeColors.TEXT_PRIMARY);
        fileCard.add(fileSec, BorderLayout.NORTH);

        JPanel fileRow = new JPanel(new BorderLayout(10, 0));
        fileRow.setOpaque(false);

        filePathField = new StyledTextField();
        filePathField.setEditable(false);

        StyledButton btnBrowse = new StyledButton("Browse File...", StyledButton.ButtonType.OUTLINE);
        btnBrowse.setIcon(IconFactory.createFileIcon(16, ThemeColors.TEXT_PRIMARY));
        btnBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFile();
            }
        });

        fileRow.add(filePathField, BorderLayout.CENTER);
        fileRow.add(btnBrowse, BorderLayout.EAST);
        fileCard.add(fileRow, BorderLayout.CENTER);

        center.add(fileCard);
        center.add(Box.createVerticalStrut(14));

        // 2. Hash Generation & Verification Card
        CardPanel hashCard = new CardPanel(new BorderLayout(0, 12), 16);
        JLabel hashSec = new JLabel("2. CRYPTOGRAPHIC DIGEST & VERIFICATION");
        hashSec.setFont(ThemeFonts.SECTION);
        hashSec.setForeground(ThemeColors.TEXT_PRIMARY);
        hashCard.add(hashSec, BorderLayout.NORTH);

        JPanel hashGrid = new JPanel(new GridBagLayout());
        hashGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Generated Hash Row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblGen = new JLabel("SHA-256 Output:");
        lblGen.setFont(ThemeFonts.BODY_BOLD);
        lblGen.setForeground(ThemeColors.TEXT_SECONDARY);
        hashGrid.add(lblGen, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        generatedHashField = new StyledTextField();
        generatedHashField.setFont(ThemeFonts.CODE);
        generatedHashField.setEditable(false);
        hashGrid.add(generatedHashField, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        StyledButton btnCopy = new StyledButton("Copy Hash", StyledButton.ButtonType.OUTLINE);
        btnCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyHash();
            }
        });
        hashGrid.add(btnCopy, gbc);

        // Expected Hash Row
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblExp = new JLabel("Expected Hash:");
        lblExp.setFont(ThemeFonts.BODY_BOLD);
        lblExp.setForeground(ThemeColors.TEXT_SECONDARY);
        hashGrid.add(lblExp, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        expectedHashField = new StyledTextField();
        expectedHashField.setFont(ThemeFonts.CODE);
        expectedHashField.setToolTipText("Paste expected SHA-256 checksum to verify integrity.");
        hashGrid.add(expectedHashField, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        StyledButton btnCompare = new StyledButton("Verify / Compare", StyledButton.ButtonType.SECONDARY);
        btnCompare.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compareHashes();
            }
        });
        hashGrid.add(btnCompare, gbc);

        // Verification Badge Row
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        matchResultBadge = new JLabel("Status: Awaiting file fingerprint calculation.");
        matchResultBadge.setFont(ThemeFonts.BODY_BOLD);
        matchResultBadge.setForeground(ThemeColors.TEXT_MUTED);
        hashGrid.add(matchResultBadge, gbc);

        hashCard.add(hashGrid, BorderLayout.CENTER);

        // Progress bar inside card
        JPanel prgBox = new JPanel(new BorderLayout(0, 4));
        prgBox.setOpaque(false);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(ThemeColors.SECONDARY);
        progressBar.setBackground(ThemeColors.INPUT_BG);

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(ThemeFonts.SMALL);
        statusLabel.setForeground(ThemeColors.TEXT_MUTED);

        prgBox.add(progressBar, BorderLayout.NORTH);
        prgBox.add(statusLabel, BorderLayout.SOUTH);
        hashCard.add(prgBox, BorderLayout.SOUTH);

        center.add(hashCard);
        center.add(Box.createVerticalStrut(14));

        // Educational Comparison Card
        CardPanel eduCard = new CardPanel(new FlowLayout(FlowLayout.LEFT, 12, 8), 10);
        eduCard.setCardBackground(new Color(25, 34, 58));
        JLabel eduLabel = new JLabel("<html><b>Security Advisory:</b> A SHA-256 hash verifies data corruption against non-malicious alterations. " +
                "However, an unkeyed hash alone does not prove authenticity if an active attacker can replace both the file and the hash digest. " +
                "For confidentiality and authenticated integrity, use AES-256-GCM (.senc) in the File Security tab.</html>");
        eduLabel.setFont(ThemeFonts.SMALL);
        eduLabel.setForeground(ThemeColors.TEXT_SECONDARY);
        eduCard.add(eduLabel);
        center.add(eduCard);

        add(center, BorderLayout.CENTER);

        // Bottom Action
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bottomBar.setOpaque(false);

        StyledButton btnCompute = new StyledButton("Calculate SHA-256 Fingerprint", StyledButton.ButtonType.PRIMARY);
        btnCompute.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateHash();
            }
        });
        bottomBar.add(btnCompute);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            generatedHashField.setText("");
            matchResultBadge.setText("File selected: " + selectedFile.getName());
            matchResultBadge.setForeground(ThemeColors.TEXT_SECONDARY);
        }
    }

    private void calculateHash() {
        if (selectedFile == null || !selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, "Please select an existing file first.", "Missing File", JOptionPane.WARNING_MESSAGE);
            return;
        }

        progressBar.setValue(0);
        statusLabel.setText("Computing 256-bit hash...");
        statusLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        HashWorker worker = new HashWorker(selectedFile, new HashWorker.HashListener() {
            @Override
            public void onProgress(int percentage) {
                progressBar.setValue(percentage);
            }

            @Override
            public void onDone(String hash, Exception error) {
                if (hash != null) {
                    progressBar.setValue(100);
                    generatedHashField.setText(hash);
                    statusLabel.setText("SHA-256 computation complete.");
                    statusLabel.setForeground(ThemeColors.SUCCESS);
                    HistoryService.getInstance().recordOperation("HASH_CALC", selectedFile.getName(), true, "NONE", selectedFile.length());

                    // Auto-compare if expected hash field has text
                    if (!expectedHashField.getText().trim().isEmpty()) {
                        compareHashes();
                    }
                } else {
                    String msg = (error != null) ? error.getMessage() : "Hashing failed.";
                    statusLabel.setText("Error: " + msg);
                    statusLabel.setForeground(ThemeColors.ERROR);
                    HistoryService.getInstance().recordOperation("HASH_CALC", selectedFile.getName(), false, "IO_ERROR", selectedFile.length());
                    JOptionPane.showMessageDialog(IntegrityLabPanel.this, msg, "Hashing Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        worker.execute();
    }

    private void compareHashes() {
        String gen = generatedHashField.getText().trim();
        String exp = expectedHashField.getText().trim();

        if (gen.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please calculate the file hash first.", "Hash Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (exp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an expected hash to compare against.", "Expected Hash Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean match = HashEngine.compareHashes(gen, exp);
        if (match) {
            matchResultBadge.setText("✔ MATCH: File checksum perfectly matches the expected cryptographic fingerprint.");
            matchResultBadge.setForeground(ThemeColors.SUCCESS);
            HistoryService.getInstance().recordOperation("HASH_VERIFY", selectedFile != null ? selectedFile.getName() : "Hash", true, "MATCH", 0);
        } else {
            matchResultBadge.setText("✖ MISMATCH: Checksum mismatch! The file may have been modified or corrupted.");
            matchResultBadge.setForeground(ThemeColors.ERROR);
            HistoryService.getInstance().recordOperation("HASH_VERIFY", selectedFile != null ? selectedFile.getName() : "Hash", false, "MISMATCH", 0);
        }
    }

    private void copyHash() {
        String hash = generatedHashField.getText().trim();
        if (!hash.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(hash), null);
            statusLabel.setText("Hash copied to clipboard.");
            statusLabel.setForeground(ThemeColors.INFO);
        }
    }
}
