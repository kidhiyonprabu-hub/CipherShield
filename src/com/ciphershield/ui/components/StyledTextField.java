package com.ciphershield.ui.components;

import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Modern dark text field with focus highlighting and rounded borders.
 */
public class StyledTextField extends JTextField {

    private boolean isFocused = false;

    public StyledTextField(int columns) {
        super(columns);
        init();
    }

    public StyledTextField() {
        super();
        init();
    }

    private void init() {
        setFont(ThemeFonts.BODY);
        setBackground(ThemeColors.INPUT_BG);
        setForeground(ThemeColors.TEXT_PRIMARY);
        setCaretColor(ThemeColors.SECONDARY);
        setSelectionColor(ThemeColors.SIDEBAR_ACTIVE);
        setSelectedTextColor(ThemeColors.TEXT_PRIMARY);
        setBorder(new EmptyBorder(8, 12, 8, 12));
        setOpaque(false);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background
        g2.setColor(ThemeColors.INPUT_BG);
        g2.fillRoundRect(0, 0, w, h, 6, 6);

        // Border
        if (isFocused) {
            g2.setColor(ThemeColors.PRIMARY);
            g2.setStroke(new BasicStroke(1.5f));
        } else {
            g2.setColor(ThemeColors.BORDER);
            g2.setStroke(new BasicStroke(1f));
        }
        g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);

        g2.dispose();
        super.paintComponent(g);
    }
}
