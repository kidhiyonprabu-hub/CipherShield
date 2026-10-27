package com.ciphershield.ui.panels;

import com.ciphershield.model.ScanResult;
import com.ciphershield.service.PrivacyScanner;
import com.ciphershield.ui.components.CardPanel;
import com.ciphershield.ui.components.FileDropHandler;
import com.ciphershield.ui.components.PageHeader;
import com.ciphershield.ui.components.StyledButton;
import com.ciphershield.ui.components.StyledTextArea;
import com.ciphershield.ui.theme.IconFactory;
import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;
import com.ciphershield.ui.theme.UiStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

/**
 * PrivacyGuard Panel: Client-side sensitive data scanner.
 * Scans text or files for PII (Emails, Phone numbers, Student IDs) prior to transmission or encryption.
 *
 * CO4: String manipulation and Regular Expressions.
 */
public class PrivacyGuardPanel extends JPanel {

    public interface ScanTransferListener {
        void onTransferToEncrypt(String text);
    }

    private final PrivacyScanner scanner = new PrivacyScanner();
    private final StyledTextArea textInputArea;
    private final DefaultTableModel tableModel;
    private final JLabel findingsCountLabel;
    private final ScanTransferListener transferListener;

    public PrivacyGuardPanel(final ScanTransferListener transferListener) {
        this.transferListener = transferListener;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        add(new PageHeader("PrivacyGuard – Sensitive Data Scanner",
                "Detect PII such as emails, phone numbers, and student IDs before encryption."),
                BorderLayout.NORTH);

        // Center: Split between Input text and Findings Table
        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);

        // Left: Text Input & Scan Actions
        CardPanel leftCard = new CardPanel(new BorderLayout(0, 10), 16);
        JLabel inTitle = new JLabel("DATA TO INSPECT (PASTE TEXT OR LOAD FILE)");
        inTitle.setFont(ThemeFonts.SECTION);
        inTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        leftCard.add(inTitle, BorderLayout.NORTH);

        textInputArea = new StyledTextArea(12, 30);
        leftCard.add(textInputArea, BorderLayout.CENTER);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        leftActions.setOpaque(false);

        StyledButton btnScanText = new StyledButton("Scan Text", StyledButton.ButtonType.PRIMARY);
        btnScanText.setIcon(IconFactory.createPrivacyIcon(16, Color.WHITE));
        btnScanText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runScan(textInputArea.getText());
            }
        });

        StyledButton btnLoadFile = new StyledButton("Scan File...", StyledButton.ButtonType.OUTLINE);
        btnLoadFile.setIcon(IconFactory.createFileIcon(16, ThemeColors.TEXT_PRIMARY));
        btnLoadFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFileAndScan();
            }
        });

        StyledButton btnClear = new StyledButton("Clear", StyledButton.ButtonType.OUTLINE);
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textInputArea.clear();
                tableModel.setRowCount(0);
                findingsCountLabel.setText("No scan executed.");
                findingsCountLabel.setForeground(ThemeColors.TEXT_MUTED);
            }
        });

        leftActions.add(btnScanText);
        leftActions.add(btnLoadFile);
        leftActions.add(btnClear);
        leftCard.add(leftActions, BorderLayout.SOUTH);

        center.add(leftCard);

        // Right: Findings Table
        CardPanel rightCard = new CardPanel(new BorderLayout(0, 10), 16);
        JPanel rightHead = new JPanel(new BorderLayout());
        rightHead.setOpaque(false);

        JLabel outTitle = new JLabel("DETECTED SENSITIVE FINDINGS");
        outTitle.setFont(ThemeFonts.SECTION);
        outTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        findingsCountLabel = new JLabel("No scan executed.");
        findingsCountLabel.setFont(ThemeFonts.SMALL);
        findingsCountLabel.setForeground(ThemeColors.TEXT_MUTED);

        rightHead.add(outTitle, BorderLayout.WEST);
        rightHead.add(findingsCountLabel, BorderLayout.EAST);
        rightCard.add(rightHead, BorderLayout.NORTH);

        String[] cols = {"Category", "Matching Value", "Line"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        UiStyles.styleTable(table);

        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setForeground(ThemeColors.WARNING);
                c.setFont(ThemeFonts.BODY_BOLD);
                c.setBackground(UiStyles.tableCellBackground(row, isSelected));
                c.setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(ThemeColors.INPUT_BG);
        rightCard.add(scrollPane, BorderLayout.CENTER);

        // Right bottom: Review & Transfer to Encrypt
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rightActions.setOpaque(false);

        StyledButton btnTransfer = new StyledButton("Review & Encrypt This Text ->", StyledButton.ButtonType.SECONDARY);
        btnTransfer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String txt = textInputArea.getText();
                if (txt != null && !txt.trim().isEmpty() && transferListener != null) {
                    transferListener.onTransferToEncrypt(txt);
                } else {
                    JOptionPane.showMessageDialog(PrivacyGuardPanel.this,
                            "No text to transfer for encryption.", "Empty Text", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        rightActions.add(btnTransfer);
        rightCard.add(rightActions, BorderLayout.SOUTH);

        center.add(rightCard);

        add(center, BorderLayout.CENTER);
        FileDropHandler.install(textInputArea, new FileDropHandler.Listener() {
            @Override
            public void onFilesDropped(List<File> files) {
                loadFileAndScan(files.get(0));
            }
        });
        FileDropHandler.install(this, new FileDropHandler.Listener() {
            @Override
            public void onFilesDropped(List<File> files) {
                loadFileAndScan(files.get(0));
            }
        });

        // Bottom Disclaimer Note
        CardPanel noteCard = new CardPanel(new FlowLayout(FlowLayout.LEFT, 12, 6), 6);
        noteCard.setCardBackground(new Color(25, 34, 58));
        JLabel noteLabel = new JLabel("<html><b>Security & Privacy Guarantee:</b> All pattern matching executes strictly on your local CPU. " +
                "Detected values are NEVER sent over the internet or saved to disk logs. Regular expression heuristics may produce false positives.</html>");
        noteLabel.setFont(ThemeFonts.SMALL);
        noteLabel.setForeground(ThemeColors.TEXT_SECONDARY);
        noteCard.add(noteLabel);
        add(noteCard, BorderLayout.SOUTH);
    }

    private void runScan(String text) {
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter or paste text to scan.", "Empty Content", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<ScanResult> results = scanner.scanText(text);
        tableModel.setRowCount(0);

        for (ScanResult r : results) {
            tableModel.addRow(new Object[]{
                    r.getCategory().getDisplayName(),
                    r.getMatchedText(),
                    "Line " + r.getLineNumber()
            });
        }

        if (results.isEmpty()) {
            findingsCountLabel.setText("✔ No sensitive PII detected");
            findingsCountLabel.setForeground(ThemeColors.SUCCESS);
        } else {
            findingsCountLabel.setText("⚠ " + results.size() + " sensitive item(s) found");
            findingsCountLabel.setForeground(ThemeColors.WARNING);
        }
    }

    private void loadFileAndScan() {
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            loadFileAndScan(chooser.getSelectedFile());
        }
    }

    private void loadFileAndScan(File file) {
        try {
            String content = scanner.readFileContent(file);
            textInputArea.setText(content);
            List<ScanResult> results = scanner.scanText(content);
            displayResults(results);
            findingsCountLabel.setText(results.isEmpty()
                    ? "✔ Clean: " + file.getName()
                    : "⚠ " + results.size() + " items in " + file.getName());
            findingsCountLabel.setForeground(results.isEmpty() ? ThemeColors.SUCCESS : ThemeColors.WARNING);
        } catch (Exception e) {
            tableModel.setRowCount(0);
            findingsCountLabel.setText("File scan failed.");
            findingsCountLabel.setForeground(ThemeColors.ERROR);
            JOptionPane.showMessageDialog(this, e.getMessage(), "File Scan Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResults(List<ScanResult> results) {
        tableModel.setRowCount(0);
        for (ScanResult result : results) {
            tableModel.addRow(new Object[]{
                    result.getCategory().getDisplayName(),
                    result.getMatchedText(),
                    "Line " + result.getLineNumber()
            });
        }
    }
}
