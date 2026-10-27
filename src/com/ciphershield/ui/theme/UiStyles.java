package com.ciphershield.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Shared Swing component styling to keep the application visually consistent.
 */
public final class UiStyles {

    private UiStyles() {
    }

    public static void styleTable(JTable table) {
        table.setFont(ThemeFonts.BODY);
        table.setRowHeight(36);
        table.setBackground(ThemeColors.INPUT_BG);
        table.setForeground(ThemeColors.TEXT_PRIMARY);
        table.setGridColor(ThemeColors.BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setSelectionBackground(ThemeColors.SIDEBAR_ACTIVE);
        table.setSelectionForeground(ThemeColors.TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(ThemeFonts.BUTTON);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setBackground(ThemeColors.CARD_BG);
        header.setForeground(ThemeColors.TEXT_SECONDARY);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setFont(ThemeFonts.BUTTON);
                label.setForeground(ThemeColors.TEXT_SECONDARY);
                label.setBackground(ThemeColors.CARD_BG);
                label.setBorder(new EmptyBorder(0, 10, 0, 10));
                return label;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                label.setForeground(ThemeColors.TEXT_PRIMARY);
                label.setBackground(isSelected ? ThemeColors.SIDEBAR_ACTIVE
                        : (row % 2 == 0 ? ThemeColors.INPUT_BG : ThemeColors.CARD_BG));
                label.setBorder(new EmptyBorder(0, 10, 0, 10));
                return label;
            }
        });
    }

    public static Color tableCellBackground(int row, boolean selected) {
        return selected ? ThemeColors.SIDEBAR_ACTIVE
                : (row % 2 == 0 ? ThemeColors.INPUT_BG : ThemeColors.CARD_BG);
    }
}
