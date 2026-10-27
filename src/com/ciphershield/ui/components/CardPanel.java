package com.ciphershield.ui.components;

import com.ciphershield.ui.theme.ThemeColors;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Reusable card panel container with rounded borders and elevated dark surface.
 */
public class CardPanel extends JPanel {

    private int cornerRadius = 12;
    private Color backgroundColor = ThemeColors.CARD_BG;
    private Color borderColor = ThemeColors.BORDER;

    public CardPanel(LayoutManager layout, int padding) {
        super(layout);
        setOpaque(false);
        setBorder(new EmptyBorder(padding, padding, padding, padding));
    }

    public CardPanel(int padding) {
        this(new BorderLayout(), padding);
    }

    public CardPanel() {
        this(16);
    }

    public void setCardBackground(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        if (h > 0) {
            g2.setPaint(new GradientPaint(0, 0, backgroundColor.brighter(), 0, h, backgroundColor));
        } else {
            g2.setColor(backgroundColor);
        }
        g2.fillRoundRect(0, 0, w, h, cornerRadius, cornerRadius);

        // Draw subtle border
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
