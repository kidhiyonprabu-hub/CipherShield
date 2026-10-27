package com.ciphershield.ui.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * Procedural icon generator using Java2D antialiased vector drawing.
 * Guarantees crisp, self-contained rendering with zero external image dependencies.
 */
public class IconFactory {

    public static ImageIcon createLogoIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Shield Path
        Path2D shield = new Path2D.Double();
        shield.moveTo(size * 0.5, size * 0.1);
        shield.curveTo(size * 0.85, size * 0.12, size * 0.9, size * 0.45, size * 0.5, size * 0.92);
        shield.curveTo(size * 0.1, size * 0.45, size * 0.15, size * 0.12, size * 0.5, size * 0.1);
        shield.closePath();

        // Gradient Fill
        GradientPaint gp = new GradientPaint(0, 0, ThemeColors.PRIMARY, size, size, ThemeColors.SECONDARY);
        g2.setPaint(gp);
        g2.fill(shield);

        // Inner glowing border
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(size * 0.04f));
        g2.draw(shield);

        // Lock Body inside Shield
        g2.setColor(Color.WHITE);
        int lockW = (int) (size * 0.35);
        int lockH = (int) (size * 0.28);
        int lockX = (int) (size * 0.5 - lockW / 2);
        int lockY = (int) (size * 0.45);
        g2.fillRoundRect(lockX, lockY, lockW, lockH, 4, 4);

        // Lock Shackle
        g2.setStroke(new BasicStroke(size * 0.06f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc((int)(size * 0.35), (int)(size * 0.28), (int)(size * 0.3), (int)(size * 0.3), 0, 180);

        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createDashboardIcon(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        int gap = 3;
        int boxW = (size - gap * 3) / 2;
        g2.fillRoundRect(gap, gap, boxW, boxW, 4, 4);
        g2.fillRoundRect(gap * 2 + boxW, gap, boxW, boxW, 4, 4);
        g2.fillRoundRect(gap, gap * 2 + boxW, boxW, boxW, 4, 4);
        g2.fillRoundRect(gap * 2 + boxW, gap * 2 + boxW, boxW, boxW, 4, 4);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createTextIcon(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawRoundRect(2, 2, size - 4, size - 4, 4, 4);
        g2.drawLine(6, 6, size - 6, 6);
        g2.drawLine(6, 11, size - 10, 11);
        g2.drawLine(6, 16, size - 12, 16);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createFileIcon(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D p = new Path2D.Double();
        p.moveTo(4, 2);
        p.lineTo(size - 8, 2);
        p.lineTo(size - 2, 8);
        p.lineTo(size - 2, size - 2);
        p.lineTo(4, size - 2);
        p.closePath();
        g2.draw(p);
        g2.drawLine(size - 8, 2, size - 8, 8);
        g2.drawLine(size - 8, 8, size - 2, 8);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createPrivacyIcon(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        // Eye shape
        g2.drawArc(2, 4, size - 4, size - 8, 45, 90);
        g2.drawArc(2, 4, size - 4, size - 8, 225, 90);
        g2.fillOval(size / 2 - 3, size / 2 - 3, 6, 6);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createBatchIcon(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(6, 2, size - 8, size - 8, 3, 3);
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 180));
        g2.drawRoundRect(4, 4, size - 8, size - 8, 3, 3);
        g2.setColor(color);
        g2.drawRoundRect(2, 6, size - 8, size - 8, 3, 3);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createIntegrityIcon(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(2, 2, size - 4, size - 4);
        // Checkmark
        g2.drawLine((int)(size * 0.28), (int)(size * 0.52), (int)(size * 0.44), (int)(size * 0.68));
        g2.drawLine((int)(size * 0.44), (int)(size * 0.68), (int)(size * 0.72), (int)(size * 0.34));
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createHistoryIcon(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(2, 2, size - 4, size - 4);
        g2.drawLine(size / 2, size / 2, size / 2, 6);
        g2.drawLine(size / 2, size / 2, size - 6, size / 2);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createAboutIcon(int size, Color color) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(2, 2, size - 4, size - 4);
        g2.fillOval(size / 2 - 1, 6, 3, 3);
        g2.drawLine(size / 2, 11, size / 2, size - 6);
        g2.dispose();
        return new ImageIcon(img);
    }

    public static ImageIcon createEyeToggleIcon(int size, boolean open) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(ThemeColors.TEXT_SECONDARY);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.drawArc(2, 3, size - 4, size - 6, 40, 100);
        g2.drawArc(2, 3, size - 4, size - 6, 220, 100);
        if (open) {
            g2.fillOval(size / 2 - 2, size / 2 - 2, 5, 5);
        } else {
            g2.drawLine(3, size - 3, size - 3, 3);
        }
        g2.dispose();
        return new ImageIcon(img);
    }
}
