package com.ciphershield.ui.panels;

import com.ciphershield.model.HistoryEntry;
import com.ciphershield.model.OperationStats;
import com.ciphershield.service.HistoryService;
import com.ciphershield.ui.components.CardPanel;
import com.ciphershield.ui.components.StatCard;
import com.ciphershield.ui.components.StyledButton;
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
import java.util.List;

/**
 * Dashboard Panel: Central monitoring station for CipherShield.
 * Displays real-time operational statistics, recent activity log, and quick navigation.
 */
public class DashboardPanel extends JPanel {

    public interface PageNavigator {
        void navigate(String pageId);
    }

    private final PageNavigator navigator;
    private final StatCard totalOpsCard;
    private final StatCard successOpsCard;
    private final StatCard failedOpsCard;
    private final StatCard successRateCard;
    private final DefaultTableModel tableModel;

    public DashboardPanel(final PageNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Top Header Banner
        JPanel topPanel = new JPanel(new BorderLayout(16, 0));
        topPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel welcomeLabel = new JLabel("CipherShield Security Center");
        welcomeLabel.setFont(ThemeFonts.TITLE);
        welcomeLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel descLabel = new JLabel("AES-256-GCM • Local security monitoring");
        descLabel.setFont(ThemeFonts.BODY);
        descLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        titleBox.add(welcomeLabel);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(descLabel);

        topPanel.add(titleBox, BorderLayout.CENTER);

        CardPanel coBadge = new CardPanel(new FlowLayout(FlowLayout.CENTER, 12, 6), 6);
        coBadge.setCardBackground(ThemeColors.BADGE_BG);
        coBadge.setBorderColor(ThemeColors.BORDER);
        JLabel coLabel = new JLabel("●  LOCAL-FIRST SECURITY");
        coLabel.setFont(ThemeFonts.SMALL);
        coLabel.setForeground(ThemeColors.SECONDARY);
        coBadge.add(coLabel);
        topPanel.add(coBadge, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // 2. Center Content (Metrics + Quick Actions + Recent Logs)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Metric Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);

        totalOpsCard = new StatCard("TOTAL OPS", "0", "All sessions",
                IconFactory.createIntegrityIcon(20, ThemeColors.INFO), ThemeColors.INFO);
        successOpsCard = new StatCard("SUCCESS", "0", "Verified",
                IconFactory.createIntegrityIcon(20, ThemeColors.SUCCESS), ThemeColors.SUCCESS);
        failedOpsCard = new StatCard("FAILED", "0", "Rejected",
                IconFactory.createIntegrityIcon(20, ThemeColors.ERROR), ThemeColors.ERROR);
        successRateCard = new StatCard("RATE", "100%", "Passed",
                IconFactory.createDashboardIcon(20, ThemeColors.SECONDARY), ThemeColors.SECONDARY);

        statsRow.add(totalOpsCard);
        statsRow.add(successOpsCard);
        statsRow.add(failedOpsCard);
        statsRow.add(successRateCard);
        centerPanel.add(statsRow);
        centerPanel.add(Box.createVerticalStrut(16));

        // Quick Access Action Cards
        JPanel quickActionsCard = new CardPanel(new BorderLayout(0, 10), 16);
        JLabel qaTitle = new JLabel("QUICK ACTIONS");
        qaTitle.setFont(ThemeFonts.SECTION);
        qaTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        quickActionsCard.add(qaTitle, BorderLayout.NORTH);

        JPanel btnGrid = new JPanel(new GridLayout(1, 4, 10, 0));
        btnGrid.setOpaque(false);

        StyledButton btnText = new StyledButton("Encrypt Text", StyledButton.ButtonType.PRIMARY);
        btnText.setToolTipText("Open Text Encryption & Decryption");
        btnText.setIcon(IconFactory.createTextIcon(16, Color.WHITE));
        btnText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (navigator != null) navigator.navigate("TEXT_CRYPTO");
            }
        });

        StyledButton btnFile = new StyledButton("Secure Files", StyledButton.ButtonType.SECONDARY);
        btnFile.setToolTipText("Open File Security (.senc)");
        btnFile.setIcon(IconFactory.createFileIcon(16, Color.BLACK));
        btnFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (navigator != null) navigator.navigate("FILE_CRYPTO");
            }
        });

        StyledButton btnPrivacy = new StyledButton("Scan Privacy", StyledButton.ButtonType.OUTLINE);
        btnPrivacy.setToolTipText("Open the PrivacyGuard scanner");
        btnPrivacy.setIcon(IconFactory.createPrivacyIcon(16, ThemeColors.SECONDARY));
        btnPrivacy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (navigator != null) navigator.navigate("PRIVACY_GUARD");
            }
        });

        StyledButton btnIntegrity = new StyledButton("Verify Hash", StyledButton.ButtonType.OUTLINE);
        btnIntegrity.setToolTipText("Open Integrity Lab (SHA-256)");
        btnIntegrity.setIcon(IconFactory.createIntegrityIcon(16, ThemeColors.SUCCESS));
        btnIntegrity.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (navigator != null) navigator.navigate("INTEGRITY_LAB");
            }
        });

        btnGrid.add(btnText);
        btnGrid.add(btnFile);
        btnGrid.add(btnPrivacy);
        btnGrid.add(btnIntegrity);
        quickActionsCard.add(btnGrid, BorderLayout.CENTER);

        centerPanel.add(quickActionsCard);
        centerPanel.add(Box.createVerticalStrut(16));

        // 3. Recent Activity Log Table Card
        CardPanel historyCard = new CardPanel(new BorderLayout(0, 10), 14);
        JPanel histHeader = new JPanel(new BorderLayout());
        histHeader.setOpaque(false);

        JLabel histTitle = new JLabel("RECENT ACTIVITY");
        histTitle.setFont(ThemeFonts.SECTION);
        histTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        StyledButton btnRefresh = new StyledButton("Refresh", StyledButton.ButtonType.OUTLINE);
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshData();
            }
        });
        histHeader.add(histTitle, BorderLayout.WEST);
        histHeader.add(btnRefresh, BorderLayout.EAST);
        historyCard.add(histHeader, BorderLayout.NORTH);

        String[] cols = {"Time", "Operation Type", "Target Entity", "Status", "Size", "Audit Note"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(tableModel);
        UiStyles.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(155);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        table.getColumnModel().getColumn(3).setPreferredWidth(125);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);

        // Status column renderer
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                c.setHorizontalAlignment(SwingConstants.CENTER);
                if ("SUCCESS".equals(value)) {
                    c.setForeground(ThemeColors.SUCCESS);
                    c.setText("● SUCCESS");
                } else {
                    c.setForeground(ThemeColors.ERROR);
                    c.setText("▲ FAILED");
                }
                c.setBackground(UiStyles.tableCellBackground(row, isSelected));
                c.setBorder(new EmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER, 1));
        scrollPane.getViewport().setBackground(ThemeColors.INPUT_BG);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        historyCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(historyCard);

        add(centerPanel, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        OperationStats stats = HistoryService.getInstance().getStats();
        totalOpsCard.updateValue(String.valueOf(stats.getTotalOperations()));
        successOpsCard.updateValue(String.valueOf(stats.getSuccessfulOperations()));
        failedOpsCard.updateValue(String.valueOf(stats.getFailedOperations()));
        successRateCard.updateValue(String.format("%.1f%%", stats.getSuccessRate()));

        // Populate recent 10 records
        tableModel.setRowCount(0);
        List<HistoryEntry> entries = HistoryService.getInstance().getHistory();
        int count = Math.min(entries.size(), 8);
        for (int i = 0; i < count; i++) {
            HistoryEntry e = entries.get(i);
            tableModel.addRow(new Object[]{
                    e.getFormattedTimestamp(),
                    e.getOperationType(),
                    e.getTargetName(),
                    e.isSuccess() ? "SUCCESS" : "FAILED",
                    e.getFormattedSize(),
                    e.getErrorCategory()
            });
        }
    }
}
