package com.ciphershield.ui.components;

import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern rounded button with hover effects and distinct color variants.
 */
public class StyledButton extends JButton {

    public enum ButtonType {
        PRIMARY(ThemeColors.PRIMARY, ThemeColors.PRIMARY_HOVER, ThemeColors.TEXT_PRIMARY),
        SECONDARY(ThemeColors.SECONDARY, ThemeColors.SECONDARY_HOVER, ThemeColors.SIDEBAR_BG),
        SUCCESS(ThemeColors.SUCCESS, new Color(110, 231, 183), ThemeColors.SIDEBAR_BG),
        DANGER(ThemeColors.ERROR, new Color(253, 164, 175), ThemeColors.TEXT_PRIMARY),
        OUTLINE(ThemeColors.BADGE_BG, ThemeColors.CARD_HOVER, ThemeColors.TEXT_PRIMARY);

        final Color normal;
        final Color hover;
        final Color text;

        ButtonType(Color normal, Color hover, Color text) {
            this.normal = normal;
            this.hover = hover;
            this.text = text;
        }
    }

    private final ButtonType type;
    private boolean isHovered = false;

    public StyledButton(String text, ButtonType type) {
        super(text);
        this.type = type;
        setFont(ThemeFonts.BUTTON);
        setForeground(type.text);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(9, 14, 9, 14));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    public StyledButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bg;
        if (!isEnabled()) {
            bg = new Color(50, 60, 80);
            g2.setColor(bg);
        } else if (isHovered) {
            bg = type.hover;
            g2.setColor(bg);
        } else {
            bg = type.normal;
            g2.setColor(bg);
        }

        if (h > 0) {
            g2.setPaint(new GradientPaint(0, 0, bg.brighter(), 0, h, bg));
        } else {
            g2.setColor(bg);
        }
        g2.fillRoundRect(0, 0, w, h, 10, 10);

        if (type == ButtonType.OUTLINE) {
            g2.setColor(ThemeColors.BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
