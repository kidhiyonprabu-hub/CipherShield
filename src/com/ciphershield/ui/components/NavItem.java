package com.ciphershield.ui.components;

import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Sidebar navigation tab button with glowing active state and hover highlights.
 */
public class NavItem extends JPanel {

    private final String id;
    private final JLabel iconLabel;
    private final JLabel textLabel;
    private boolean isActive = false;
    private boolean isHovered = false;
    private Runnable clickAction;

    public NavItem(String id, String title, Icon icon) {
        this.id = id;
        setLayout(new BorderLayout(12, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 16, 12, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setFocusable(true);
        setToolTipText(title);

        iconLabel = new JLabel(icon);
        textLabel = new JLabel(title);
        textLabel.setFont(ThemeFonts.BODY);
        textLabel.setForeground(ThemeColors.TEXT_SECONDARY);

        add(iconLabel, BorderLayout.WEST);
        add(textLabel, BorderLayout.CENTER);

        MouseAdapter navigationMouseListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                activate();
            }
        };
        addMouseListener(navigationMouseListener);
        iconLabel.addMouseListener(navigationMouseListener);
        textLabel.addMouseListener(navigationMouseListener);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    activate();
                }
            }
        });
    }

    private void activate() {
        if (clickAction != null) {
            clickAction.run();
        }
    }

    public String getId() { return id; }

    public void setClickAction(Runnable action) {
        this.clickAction = action;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (active) {
            textLabel.setFont(ThemeFonts.BODY_BOLD);
            textLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        } else {
            textLabel.setFont(ThemeFonts.BODY);
            textLabel.setForeground(ThemeColors.TEXT_SECONDARY);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (isActive) {
            // Background fill
            g2.setColor(ThemeColors.SIDEBAR_ACTIVE);
            g2.fillRoundRect(6, 2, w - 12, h - 4, 8, 8);

            // Active accent bar on left
            g2.setColor(ThemeColors.PRIMARY);
            g2.fillRoundRect(0, 4, 4, h - 8, 2, 2);
        } else if (isHovered) {
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillRoundRect(6, 2, w - 12, h - 4, 8, 8);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
