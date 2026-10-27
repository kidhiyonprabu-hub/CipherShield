package com.ciphershield.ui.panels;

import com.ciphershield.crypto.CryptoEngine;
import com.ciphershield.exception.DecryptionException;
import com.ciphershield.exception.ValidationException;
import com.ciphershield.service.HistoryService;
import com.ciphershield.ui.components.CardPanel;
import com.ciphershield.ui.components.PageHeader;
import com.ciphershield.ui.components.StyledButton;
import com.ciphershield.ui.components.StyledPasswordField;
import com.ciphershield.ui.components.StyledTextArea;
import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * Text Encryption & Decryption Panel.
 * Uses AES-256-GCM with PBKDF2WithHmacSHA256 key derivation.
 * Includes password confirmation during encryption, export, copy, and clear controls.
 */
public class TextCryptoPanel extends JPanel {

    private final CryptoEngine cryptoEngine = new CryptoEngine();
    private final StyledTextArea inputArea;
    private final StyledTextArea outputArea;
    private final StyledPasswordField passwordField;
    private final StyledPasswordField confirmPasswordField;
    private final JLabel confirmLabel;
    private final JLabel statusBadge;

    public TextCryptoPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        add(new PageHeader("Text Encryption & Decryption (AES-256-GCM)",
                "Authenticated encryption with PBKDF2 (310,000 rounds) and Base64 transport encoding."),
                BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);

        // Left: Input Card
        CardPanel leftCard = new CardPanel(new BorderLayout(0, 10), 16);
        JLabel inLabel = new JLabel("INPUT TEXT / CIPHERTEXT");
        inLabel.setFont(ThemeFonts.SECTION);
        inLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        leftCard.add(inLabel, BorderLayout.NORTH);

        inputArea = new StyledTextArea(12, 30);
        leftCard.add(inputArea, BorderLayout.CENTER);

        // Password controls at bottom of left card
        JPanel passPanel = new JPanel();
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.Y_AXIS));
        passPanel.setOpaque(false);

        JLabel passLabel = new JLabel("Secret Password:");
        passLabel.setFont(ThemeFonts.SMALL);
        passLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        passwordField = new StyledPasswordField();

        confirmLabel = new JLabel("Confirm Password (Required for Encryption):");
        confirmLabel.setFont(ThemeFonts.SMALL);
        confirmLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        confirmPasswordField = new StyledPasswordField();

        passPanel.add(Box.createVerticalStrut(8));
        passPanel.add(passLabel);
        passPanel.add(Box.createVerticalStrut(4));
        passPanel.add(passwordField);
        passPanel.add(Box.createVerticalStrut(6));
        passPanel.add(confirmLabel);
        passPanel.add(Box.createVerticalStrut(4));
        passPanel.add(confirmPasswordField);

        leftCard.add(passPanel, BorderLayout.SOUTH);
        center.add(leftCard);

        // Right: Output Card
        CardPanel rightCard = new CardPanel(new BorderLayout(0, 10), 16);
        JLabel outLabel = new JLabel("OUTPUT RESULT");
        outLabel.setFont(ThemeFonts.SECTION);
        outLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        rightCard.add(outLabel, BorderLayout.NORTH);

        outputArea = new StyledTextArea(12, 30);
        outputArea.setEditable(false);
        rightCard.add(outputArea, BorderLayout.CENTER);

        // Output Action Buttons
        JPanel outActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        outActions.setOpaque(false);

        StyledButton btnCopy = new StyledButton("Copy Output", StyledButton.ButtonType.OUTLINE);
        btnCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyOutput();
            }
        });

        StyledButton btnExport = new StyledButton("Export to File", StyledButton.ButtonType.OUTLINE);
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportOutput();
            }
        });

        StyledButton btnClear = new StyledButton("Clear All", StyledButton.ButtonType.OUTLINE);
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputArea.clear();
                outputArea.clear();
                passwordField.clearPassword();
                confirmPasswordField.clearPassword();
                setStatus("Ready", ThemeColors.TEXT_MUTED);
            }
        });

        outActions.add(btnCopy);
        outActions.add(btnExport);
        outActions.add(btnClear);
        rightCard.add(outActions, BorderLayout.SOUTH);

        center.add(rightCard);
        add(center, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout(16, 0));
        bottomBar.setOpaque(false);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);

        StyledButton btnEncrypt = new StyledButton("Encrypt Text (AES-GCM)", StyledButton.ButtonType.PRIMARY);
        btnEncrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEncrypt();
            }
        });

        StyledButton btnDecrypt = new StyledButton("Decrypt Ciphertext", StyledButton.ButtonType.SECONDARY);
        btnDecrypt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDecrypt();
            }
        });

        btnRow.add(btnEncrypt);
        btnRow.add(btnDecrypt);

        statusBadge = new JLabel("Ready. Enter text and password to begin.");
        statusBadge.setFont(ThemeFonts.BODY);
        statusBadge.setForeground(ThemeColors.TEXT_SECONDARY);

        bottomBar.add(btnRow, BorderLayout.WEST);
        bottomBar.add(statusBadge, BorderLayout.CENTER);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private void handleEncrypt() {
        String input = inputArea.getText();
        char[] pass = passwordField.getPassword();
        char[] confirm = confirmPasswordField.getPassword();

        if (input == null || input.trim().isEmpty()) {
            clearCredentials(pass, confirm);
            showError("Please enter text to encrypt.");
            return;
        }
        if (pass.length == 0) {
            clearCredentials(pass, confirm);
            showError("Password cannot be empty.");
            return;
        }
        if (!Arrays.equals(pass, confirm)) {
            clearCredentials(pass, confirm);
            showError("Password confirmation does not match.");
            return;
        }

        try {
            String encrypted = cryptoEngine.encryptText(input, pass);
            outputArea.setText(encrypted);
            setStatus("Encryption Successful! AES-256-GCM ciphertext generated.", ThemeColors.SUCCESS);
            HistoryService.getInstance().recordOperation("TEXT_ENCRYPT", "Text (chars=" + input.length() + ")", true, "NONE", input.length());
        } catch (Exception e) {
            showError("Encryption failed: " + e.getMessage());
            HistoryService.getInstance().recordOperation("TEXT_ENCRYPT", "Text", false, e.getClass().getSimpleName(), input.length());
        } finally {
            clearCredentials(pass, confirm);
        }
    }

    private void handleDecrypt() {
        String input = inputArea.getText();
        char[] pass = passwordField.getPassword();

        if (input == null || input.trim().isEmpty()) {
            clearCredentials(pass);
            showError("Please enter Base64 ciphertext to decrypt.");
            return;
        }
        if (pass.length == 0) {
            clearCredentials(pass);
            showError("Please enter the decryption password.");
            return;
        }

        try {
            String decrypted = cryptoEngine.decryptText(input, pass);
            outputArea.setText(decrypted);
            setStatus("Decryption Successful! Plaintext restored and authenticated.", ThemeColors.SUCCESS);
            HistoryService.getInstance().recordOperation("TEXT_DECRYPT", "Ciphertext", true, "NONE", input.length());
        } catch (DecryptionException e) {
            showError(e.getMessage());
            HistoryService.getInstance().recordOperation("TEXT_DECRYPT", "Ciphertext", false, "AUTH_FAILED", input.length());
        } catch (ValidationException e) {
            showError("Validation Error: " + e.getMessage());
            HistoryService.getInstance().recordOperation("TEXT_DECRYPT", "Ciphertext", false, "VALIDATION_FAILED", input.length());
        } catch (Exception e) {
            showError("Decryption failed: " + e.getMessage());
            HistoryService.getInstance().recordOperation("TEXT_DECRYPT", "Ciphertext", false, e.getClass().getSimpleName(), input.length());
        } finally {
            clearCredentials(pass);
        }
    }

    private void clearCredentials(char[]... credentials) {
        for (char[] credential : credentials) {
            Arrays.fill(credential, '\0');
        }
        passwordField.clearPassword();
        confirmPasswordField.clearPassword();
    }

    private void copyOutput() {
        String text = outputArea.getText();
        if (text != null && !text.isEmpty()) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
            setStatus("Output copied to clipboard!", ThemeColors.INFO);
        } else {
            showError("No output to copy.");
        }
    }

    private void exportOutput() {
        String text = outputArea.getText();
        if (text == null || text.isEmpty()) {
            showError("No output to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("CipherShield_Output.txt"));
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(target)) {
                fos.write(text.getBytes("UTF-8"));
                setStatus("Exported successfully to: " + target.getName(), ThemeColors.SUCCESS);
            } catch (Exception e) {
                showError("Export failed: " + e.getMessage());
            }
        }
    }

    private void setStatus(String message, Color color) {
        statusBadge.setText(message);
        statusBadge.setForeground(color);
    }

    private void showError(String message) {
        setStatus("Error: " + message, ThemeColors.ERROR);
        JOptionPane.showMessageDialog(this, message, "CipherShield Error", JOptionPane.ERROR_MESSAGE);
    }
}
