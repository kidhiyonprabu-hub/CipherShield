package com.ciphershield.ui.panels;

import com.ciphershield.model.HistoryEntry;
import com.ciphershield.service.HistoryService;
import com.ciphershield.ui.components.CardPanel;
import com.ciphershield.ui.components.PageHeader;
import com.ciphershield.ui.components.StyledButton;
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
import java.util.List;

/**
 * Security Audit History Panel.
 * Logs all cryptographic actions securely without storing sensitive keys, passwords, or plaintexts.
 *
 * CO4: File persistence of audit trails.
 * CO5: Interactive Swing Table & Dialogs.
 */
public class HistoryPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JLabel totalCountLabel;

    public HistoryPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        add(new PageHeader("Security History & Audit Trail",
                "Review cryptographic activity. Passwords, keys, and plaintext are never stored."),
                BorderLayout.NORTH);

        // Center Content Card
        CardPanel card = new CardPanel(new BorderLayout(0, 12), 16);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        totalCountLabel = new JLabel("0 records logged.");
        totalCountLabel.setFont(ThemeFonts.SECTION);
        totalCountLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JPanel btnBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBox.setOpaque(false);

        StyledButton btnRefresh = new StyledButton("Refresh", StyledButton.ButtonType.OUTLINE);
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadHistoryData();
            }
        });

        StyledButton btnClear = new StyledButton("Clear Audit Log", StyledButton.ButtonType.DANGER);
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmClearHistory();
            }
        });

        btnBox.add(btnRefresh);
        btnBox.add(btnClear);

        topBar.add(totalCountLabel, BorderLayout.WEST);
        topBar.add(btnBox, BorderLayout.EAST);
        card.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Session ID", "Timestamp", "Operation", "Target Resource", "Result Status", "Payload Size", "Audit Category"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        UiStyles.styleTable(table);

        // Result column renderer
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                if ("SUCCESS".equals(value)) {
                    c.setForeground(ThemeColors.SUCCESS);
                    c.setText("✔ SUCCESS");
                } else {
                    c.setForeground(ThemeColors.ERROR);
                    c.setText("✖ FAILED");
                }
                c.setBackground(UiStyles.tableCellBackground(row, isSelected));
                c.setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER));
        scrollPane.getViewport().setBackground(ThemeColors.INPUT_BG);
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        // Bottom Security Note
        CardPanel noteCard = new CardPanel(new FlowLayout(FlowLayout.LEFT, 12, 6), 6);
        noteCard.setCardBackground(new Color(25, 34, 58));
        JLabel note = new JLabel("<html><b>Compliance Notice:</b> Operation histories are stored in ~/.ciphershield/history.dat. " +
                "This file is local to your machine. For forensic auditing, note that local files can be modified by root/admin accounts.</html>");
        note.setFont(ThemeFonts.SMALL);
        note.setForeground(ThemeColors.TEXT_SECONDARY);
        noteCard.add(note);
        add(noteCard, BorderLayout.SOUTH);

        loadHistoryData();
    }

    public void loadHistoryData() {
        tableModel.setRowCount(0);
        List<HistoryEntry> list = HistoryService.getInstance().getHistory();
        for (HistoryEntry e : list) {
            tableModel.addRow(new Object[]{
                    e.getId(),
                    e.getFormattedTimestamp(),
                    e.getOperationType(),
                    e.getTargetName(),
                    e.isSuccess() ? "SUCCESS" : "FAILED",
                    e.getFormattedSize(),
                    e.getErrorCategory()
            });
        }
        totalCountLabel.setText(list.size() + " total audit record(s).");
    }

    private void confirmClearHistory() {
        int res = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently erase the local security history?\nThis action cannot be undone.",
                "Confirm Clear Audit Log",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            HistoryService.getInstance().clearHistory();
            loadHistoryData();
            JOptionPane.showMessageDialog(this, "Audit history cleared.", "History Erased", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
