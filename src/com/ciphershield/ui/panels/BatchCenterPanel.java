package com.ciphershield.ui.panels;

import com.ciphershield.model.BatchFileItem;
import com.ciphershield.service.BatchProcessor;
import com.ciphershield.ui.components.CardPanel;
import com.ciphershield.ui.components.FileDropHandler;
import com.ciphershield.ui.components.PageHeader;
import com.ciphershield.ui.components.StyledButton;
import com.ciphershield.ui.components.StyledPasswordField;
import com.ciphershield.ui.components.StyledTextField;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Batch Center Panel: Multi-file encryption and decryption engine.
 * Employs a bounded background thread pool to prevent disk freezing while keeping GUI fully responsive.
 *
 * CO3: Multithreading, Thread Pools, and Exception Handling.
 * CO4: File I/O for multiple assets.
 * CO5: Interactive event-driven GUI.
 */
public class BatchCenterPanel extends JPanel {

    private final List<BatchFileItem> queueItems = new ArrayList<>();
    private final BatchProcessor batchProcessor = new BatchProcessor();

    private final DefaultTableModel tableModel;
    private final StyledTextField outputDirField;
    private final StyledPasswordField passwordField;
    private final JRadioButton radioEncrypt;
    private final JRadioButton radioDecrypt;
    private final JProgressBar overallProgress;
    private final JLabel statusLabel;
    private final StyledButton btnStart;
    private final StyledButton btnCancel;
    private final StyledButton btnAddFiles;
    private final StyledButton btnClearQueue;
    private File selectedOutputDir;

    public BatchCenterPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        add(new PageHeader("Batch Center – Bulk File Processing",
                "Process files concurrently with bounded workers and per-file fault isolation."),
                BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setOpaque(false);

        // Top Controls Card
        CardPanel configCard = new CardPanel(new BorderLayout(0, 10), 14);
        JLabel cfgTitle = new JLabel("CONFIGURATION & TARGETS");
        cfgTitle.setFont(ThemeFonts.SECTION);
        cfgTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        configCard.add(cfgTitle, BorderLayout.NORTH);

        JPanel cfgForm = new JPanel(new GridBagLayout());
        cfgForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Operation type row
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel lblOp = new JLabel("Operation:");
        lblOp.setFont(ThemeFonts.BODY_BOLD);
        lblOp.setForeground(ThemeColors.TEXT_SECONDARY);
        cfgForm.add(lblOp, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        radioPanel.setOpaque(false);
        radioEncrypt = new JRadioButton("Batch Encrypt (.senc)", true);
        radioEncrypt.setFont(ThemeFonts.BODY);
        radioEncrypt.setForeground(ThemeColors.TEXT_PRIMARY);
        radioEncrypt.setOpaque(false);

        radioDecrypt = new JRadioButton("Batch Decrypt (.senc -> Plain)", false);
        radioDecrypt.setFont(ThemeFonts.BODY);
        radioDecrypt.setForeground(ThemeColors.TEXT_PRIMARY);
        radioDecrypt.setOpaque(false);

        ButtonGroup grp = new ButtonGroup();
        grp.add(radioEncrypt);
        grp.add(radioDecrypt);
        radioPanel.add(radioEncrypt);
        radioPanel.add(radioDecrypt);
        cfgForm.add(radioPanel, gbc);

        // Destination row
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel lblDst = new JLabel("Output Directory:");
        lblDst.setFont(ThemeFonts.BODY_BOLD);
        lblDst.setForeground(ThemeColors.TEXT_SECONDARY);
        cfgForm.add(lblDst, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        outputDirField = new StyledTextField();
        outputDirField.setEditable(false);
        cfgForm.add(outputDirField, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        StyledButton btnChooseDir = new StyledButton("Choose Dir...", StyledButton.ButtonType.OUTLINE);
        btnChooseDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseOutputDir();
            }
        });
        cfgForm.add(btnChooseDir, gbc);

        // Password row
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel lblPass = new JLabel("Batch Password:");
        lblPass.setFont(ThemeFonts.BODY_BOLD);
        lblPass.setForeground(ThemeColors.TEXT_SECONDARY);
        cfgForm.add(lblPass, gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        passwordField = new StyledPasswordField();
        cfgForm.add(passwordField, gbc);

        configCard.add(cfgForm, BorderLayout.CENTER);
        center.add(configCard, BorderLayout.NORTH);

        // Files Table Card
        CardPanel queueCard = new CardPanel(new BorderLayout(0, 10), 14);
        JPanel queueHeader = new JPanel(new BorderLayout());
        queueHeader.setOpaque(false);

        JLabel qTitle = new JLabel("BATCH QUEUE");
        qTitle.setFont(ThemeFonts.SECTION);
        qTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JPanel qBtnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        qBtnBox.setOpaque(false);

        btnAddFiles = new StyledButton("Add Files...", StyledButton.ButtonType.OUTLINE);
        btnAddFiles.setIcon(IconFactory.createFileIcon(16, ThemeColors.TEXT_PRIMARY));
        btnAddFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addFilesToQueue();
            }
        });

        btnClearQueue = new StyledButton("Clear Queue", StyledButton.ButtonType.OUTLINE);
        btnClearQueue.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queueItems.clear();
                tableModel.setRowCount(0);
                updateStatusLabel();
            }
        });

        qBtnBox.add(btnAddFiles);
        qBtnBox.add(btnClearQueue);

        queueHeader.add(qTitle, BorderLayout.WEST);
        queueHeader.add(qBtnBox, BorderLayout.EAST);
        queueCard.add(queueHeader, BorderLayout.NORTH);

        String[] cols = {"File Name", "Size", "Current Status", "Details / Error"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        UiStyles.styleTable(table);

        // Status column renderer
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                String val = String.valueOf(value);
                if (val.contains("SUCCESS")) {
                    c.setForeground(ThemeColors.SUCCESS);
                } else if (val.contains("FAILED")) {
                    c.setForeground(ThemeColors.ERROR);
                } else if (val.contains("PROCESSING")) {
                    c.setForeground(ThemeColors.SECONDARY);
                } else {
                    c.setForeground(ThemeColors.TEXT_MUTED);
                }
                c.setBackground(UiStyles.tableCellBackground(row, isSelected));
                c.setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(ThemeColors.INPUT_BG);
        FileDropHandler.install(table, new FileDropHandler.Listener() {
            @Override
            public void onFilesDropped(List<File> files) {
                addFilesToQueue(files.toArray(new File[0]));
            }
        });
        queueCard.add(scrollPane, BorderLayout.CENTER);

        center.add(queueCard, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // Bottom Progress & Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout(0, 10));
        bottomBar.setOpaque(false);

        JPanel prgBox = new JPanel(new BorderLayout(0, 4));
        prgBox.setOpaque(false);

        overallProgress = new JProgressBar(0, 100);
        overallProgress.setStringPainted(true);
        overallProgress.setForeground(ThemeColors.PRIMARY);
        overallProgress.setBackground(ThemeColors.INPUT_BG);
        overallProgress.setPreferredSize(new Dimension(800, 22));

        statusLabel = new JLabel("0 files queued. Ready.");
        statusLabel.setFont(ThemeFonts.BODY);
        statusLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        prgBox.add(overallProgress, BorderLayout.NORTH);
        prgBox.add(statusLabel, BorderLayout.SOUTH);
        bottomBar.add(prgBox, BorderLayout.CENTER);

        JPanel actBtnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actBtnBox.setOpaque(false);

        btnStart = new StyledButton("Start Batch Processing", StyledButton.ButtonType.PRIMARY);
        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startBatchExecution();
            }
        });

        btnCancel = new StyledButton("Cancel Batch", StyledButton.ButtonType.DANGER);
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                batchProcessor.cancel();
                statusLabel.setText("Cancelling remaining files...");
                statusLabel.setForeground(ThemeColors.WARNING);
            }
        });

        actBtnBox.add(btnStart);
        actBtnBox.add(btnCancel);
        bottomBar.add(actBtnBox, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private void chooseOutputDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            selectedOutputDir = chooser.getSelectedFile();
            outputDirField.setText(selectedOutputDir.getAbsolutePath());
        }
    }

    private void addFilesToQueue() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            addFilesToQueue(chooser.getSelectedFiles());
        }
    }

    private void addFilesToQueue(File[] files) {
        if (!btnStart.isEnabled()) {
            return;
        }
        int addedCount = 0;
        File firstAdded = null;
        for (File file : files) {
            if (file == null || !file.isFile()) {
                continue;
            }
            BatchFileItem item = new BatchFileItem(file);
            queueItems.add(item);
            tableModel.addRow(new Object[]{
                    item.getFileName(),
                    item.getFormattedSize(),
                    item.getStatus().getDisplay(),
                    ""
            });
            if (firstAdded == null) {
                firstAdded = file;
            }
            addedCount++;
        }
        if (selectedOutputDir == null && firstAdded != null) {
            selectedOutputDir = firstAdded.getParentFile();
            outputDirField.setText(selectedOutputDir.getAbsolutePath());
        }
        if (addedCount == 0) {
            JOptionPane.showMessageDialog(this, "No regular files were selected or dropped.",
                    "No Files Added", JOptionPane.WARNING_MESSAGE);
        }
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        statusLabel.setText(queueItems.size() + " file(s) in queue.");
        statusLabel.setForeground(ThemeColors.TEXT_SECONDARY);
    }

    private void startBatchExecution() {
        if (queueItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Queue is empty. Please add files first.", "No Files", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedOutputDir == null || !selectedOutputDir.isDirectory() || !selectedOutputDir.canWrite()) {
            JOptionPane.showMessageDialog(this, "Please select a valid output directory.", "Missing Directory", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final char[] pass = passwordField.getPassword();
        if (pass.length == 0) {
            JOptionPane.showMessageDialog(this, "Please specify a password for batch processing.", "Empty Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        passwordField.clearPassword();

        btnStart.setEnabled(false);
        btnCancel.setEnabled(true);
        overallProgress.setValue(0);

        final boolean isEncrypt = radioEncrypt.isSelected();
        for (int i = 0; i < queueItems.size(); i++) {
            queueItems.get(i).setStatus(BatchFileItem.Status.PENDING);
            queueItems.get(i).setErrorMessage("");
            tableModel.setValueAt(BatchFileItem.Status.PENDING.getDisplay(), i, 2);
            tableModel.setValueAt("", i, 3);
        }
        btnAddFiles.setEnabled(false);
        btnClearQueue.setEnabled(false);

        batchProcessor.executeBatch(queueItems, pass, isEncrypt, selectedOutputDir, new BatchProcessor.BatchCallback() {
            @Override
            public void onFileStart(final BatchFileItem item, final int index, final int total) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int row = index - 1;
                        tableModel.setValueAt("Processing...", row, 2);
                        int pct = (int) (((index - 1) * 100.0) / total);
                        overallProgress.setValue(pct);
                        statusLabel.setText("Processing file " + index + " of " + total + ": " + item.getFileName());
                    }
                });
            }

            @Override
            public void onFileComplete(final BatchFileItem item, final int index, final int total) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int row = index - 1;
                        tableModel.setValueAt("✔ SUCCESS", row, 2);
                        tableModel.setValueAt("Saved: " + item.getOutputFile().getName(), row, 3);
                        int pct = (int) ((index * 100.0) / total);
                        overallProgress.setValue(pct);
                    }
                });
            }

            @Override
            public void onFileFailed(final BatchFileItem item, final int index, final int total, final String error) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        int row = index - 1;
                        tableModel.setValueAt("▲ FAILED", row, 2);
                        tableModel.setValueAt(error, row, 3);
                    }
                });
            }

            @Override
            public void onBatchFinished(final int successCount, final int failCount, final boolean cancelled) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Arrays.fill(pass, '\0');
                        passwordField.clearPassword();
                        btnStart.setEnabled(true);
                        btnCancel.setEnabled(false);
                        btnAddFiles.setEnabled(true);
                        btnClearQueue.setEnabled(true);
                        overallProgress.setValue(100);

                        String summary = "Batch Complete! Success: " + successCount + ", Failed: " + failCount;
                        if (cancelled) summary += " (Cancelled by user)";
                        statusLabel.setText(summary);
                        statusLabel.setForeground(failCount > 0 ? ThemeColors.WARNING : ThemeColors.SUCCESS);

                        JOptionPane.showMessageDialog(BatchCenterPanel.this, summary, "Batch Execution Finished",
                                failCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        });
    }
}
