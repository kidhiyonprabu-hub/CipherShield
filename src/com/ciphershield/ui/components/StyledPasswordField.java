package com.ciphershield.ui.components;

import com.ciphershield.ui.theme.IconFactory;
import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Custom password input field with integrated visibility eye toggle.
 */
public class StyledPasswordField extends JPanel {

    private final JPasswordField passwordField;
    private final JButton toggleButton;
    private boolean isVisible = false;
    private boolean isFocused = false;
    private final char defaultEchoChar;

    public StyledPasswordField() {
        setLayout(new BorderLayout());
        setOpaque(false);
        setBackground(ThemeColors.INPUT_BG);

        passwordField = new JPasswordField();
        passwordField.setFont(ThemeFonts.BODY);
        passwordField.setBackground(ThemeColors.INPUT_BG);
        passwordField.setForeground(ThemeColors.TEXT_PRIMARY);
        passwordField.setCaretColor(ThemeColors.SECONDARY);
        passwordField.setBorder(new EmptyBorder(8, 12, 8, 8));
        passwordField.setOpaque(false);
        defaultEchoChar = passwordField.getEchoChar();

        toggleButton = new JButton();
        toggleButton.setIcon(IconFactory.createEyeToggleIcon(16, false));
        toggleButton.setFocusPainted(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.setToolTipText("Show/Hide Password");
        toggleButton.setBorder(new EmptyBorder(0, 4, 0, 8));

        toggleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isVisible = !isVisible;
                if (isVisible) {
                    passwordField.setEchoChar((char) 0);
                    toggleButton.setIcon(IconFactory.createEyeToggleIcon(16, true));
                } else {
                    passwordField.setEchoChar(defaultEchoChar);
                    toggleButton.setIcon(IconFactory.createEyeToggleIcon(16, false));
                }
            }
        });

        passwordField.addFocusListener(new FocusAdapter() {
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

        add(passwordField, BorderLayout.CENTER);
        add(toggleButton, BorderLayout.EAST);
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public void clearPassword() {
        passwordField.setText("");
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(ThemeColors.INPUT_BG);
        g2.fillRoundRect(0, 0, w, h, 6, 6);

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
