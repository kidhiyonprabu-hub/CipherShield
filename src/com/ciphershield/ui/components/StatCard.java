package com.ciphershield.ui.components;

import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Metric card for displaying operational statistics on the Dashboard.
 */
public class StatCard extends JPanel {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtitle, Icon icon, Color accentColor) {
        this.accentColor = accentColor;
        setLayout(new BorderLayout(12, 6));
        setOpaque(false);
        setPreferredSize(new Dimension(220, 120));
        setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeFonts.SMALL);
        titleLabel.setForeground(ThemeColors.TEXT_SECONDARY);
        header.add(titleLabel, BorderLayout.CENTER);

        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setVerticalAlignment(SwingConstants.TOP);
            header.add(iconLabel, BorderLayout.EAST);
        }

        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(ThemeFonts.STAT_VALUE);
        valueLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(ThemeFonts.SMALL);
        subtitleLabel.setForeground(ThemeColors.TEXT_MUTED);

        textPanel.add(header);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(valueLabel);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(subtitleLabel);

        add(textPanel, BorderLayout.CENTER);
    }

    public void updateValue(String newValue) {
        valueLabel.setText(newValue);
        repaint();
    }

    public void updateSubtitle(String newSubtitle) {
        subtitleLabel.setText(newSubtitle);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background
        if (h > 0) {
            g2.setPaint(new GradientPaint(0, 0, ThemeColors.CARD_HOVER, 0, h, ThemeColors.CARD_BG));
        } else {
            g2.setColor(ThemeColors.CARD_BG);
        }
        g2.fillRoundRect(0, 0, w, h, 10, 10);

        // Accent indicator bar on the left
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 4, 4, h - 8, 3, 3);

        // Border
        g2.setColor(ThemeColors.BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

        g2.dispose();
        super.paintComponent(g);
    }
}
