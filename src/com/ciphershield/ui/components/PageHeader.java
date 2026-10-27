package com.ciphershield.ui.components;

import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import java.awt.*;

/**
 * Consistent page title and description block for tool screens.
 */
public class PageHeader extends JPanel {

    public PageHeader(String title, String description) {
        super(new BorderLayout(12, 0));
        setOpaque(false);

        JPanel accent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int height = getHeight();
                if (height > 0) {
                    g2.setPaint(new GradientPaint(0, 0, ThemeColors.SECONDARY, 0, height, ThemeColors.PRIMARY));
                    g2.fillRoundRect(0, 0, getWidth(), height, 4, 4);
                }
                g2.dispose();
            }
        };
        accent.setOpaque(false);
        accent.setPreferredSize(new Dimension(4, 52));
        add(accent, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(ThemeFonts.TITLE);
        titleLabel.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setFont(ThemeFonts.SMALL);
        descriptionLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(descriptionLabel);
        add(text, BorderLayout.CENTER);
    }
}
