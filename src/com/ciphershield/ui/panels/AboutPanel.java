package com.ciphershield.ui.panels;

import com.ciphershield.ui.components.CardPanel;
import com.ciphershield.ui.theme.IconFactory;
import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * About Panel: Comprehensive project documentation, Course Outcomes (CO3, CO4, CO5) mapping,
 * and cryptographic architecture specifications.
 */
public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JLabel logo = new JLabel(IconFactory.createLogoIcon(56));
        header.add(logo, BorderLayout.WEST);

        JPanel hText = new JPanel();
        hText.setLayout(new BoxLayout(hText, BoxLayout.Y_AXIS));
        hText.setOpaque(false);

        JLabel title = new JLabel("CipherShield Desktop – v1.0.0");
        title.setFont(ThemeFonts.TITLE);
        title.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel sub = new JLabel("Secure Encryption and Decryption Tool with Robust Error Handling");
        sub.setFont(ThemeFonts.SUBTITLE);
        sub.setForeground(ThemeColors.SECONDARY);

        JLabel meta = new JLabel("Academic Micro Project 03 • Cyber Security & Software Tools Domain");
        meta.setFont(ThemeFonts.SMALL);
        meta.setForeground(ThemeColors.TEXT_MUTED);

        hText.add(title);
        hText.add(Box.createVerticalStrut(2));
        hText.add(sub);
        hText.add(Box.createVerticalStrut(4));
        hText.add(meta);

        header.add(hText, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Center Content - Scrollable Cards
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        centerGrid.setOpaque(false);

        // Left Card: Course Outcomes (CO3, CO4, CO5) Mapping
        CardPanel coCard = new CardPanel(new BorderLayout(0, 12), 16);
        JLabel coTitle = new JLabel("INTEGRATED COURSE OUTCOMES (CO)");
        coTitle.setFont(ThemeFonts.SECTION);
        coTitle.setForeground(ThemeColors.SECONDARY);
        coCard.add(coTitle, BorderLayout.NORTH);

        JEditorPane coPane = new JEditorPane();
        coPane.setContentType("text/html");
        coPane.setEditable(false);
        coPane.setOpaque(false);
        coPane.setText("<html><body style='font-family: Segoe UI, sans-serif; font-size: 11pt; color: #cbd5e1;'>" +
                "<div style='margin-bottom: 14px;'>" +
                "<b style='color: #a78bfa; font-size: 12pt;'>CO3: Multithreading & Exception Handling</b><br/>" +
                "• <b>Background Workers:</b> <code>SwingWorker</code> handles intensive file encryption and hashing without UI thread freeze.<br/>" +
                "• <b>Bounded Thread Pool:</b> <code>BatchProcessor</code> runs multi-file jobs safely with max 2 concurrent threads.<br/>" +
                "• <b>Hierarchy:</b> Custom <code>CipherShieldException</code>, <code>DecryptionException</code>, <code>FileFormatException</code>." +
                "</div>" +
                "<div style='margin-bottom: 14px;'>" +
                "<b style='color: #38bdf8; font-size: 12pt;'>CO4: File I/O & String Manipulation</b><br/>" +
                "• <b>Binary Containers:</b> Custom <code>.senc</code> format with magic header <code>CSHD</code> and versioning.<br/>" +
                "• <b>Atomic Writes:</b> Safe write-to-temp and atomic rename preventing partial file loss.<br/>" +
                "• <b>PrivacyGuard Regex:</b> Regex scanning for emails, phone numbers, and student IDs.<br/>" +
                "• <b>SHA-256 Hashing:</b> Chunked streaming stream processing of multi-megabyte files." +
                "</div>" +
                "<div>" +
                "<b style='color: #34d399; font-size: 12pt;'>CO5: Graphical User Interfaces & Event Programming</b><br/>" +
                "• <b>Custom Swing Architecture:</b> High-contrast cybersecurity dark theme.<br/>" +
                "• <b>Event Dispatch Model:</b> Decoupled action listeners and real-time progress callbacks.<br/>" +
                "• <b>Zero External Assets:</b> Procedural vector graphics using <code>Graphics2D</code>." +
                "</div>" +
                "</body></html>");
        coCard.add(coPane, BorderLayout.CENTER);
        centerGrid.add(coCard);

        // Right Card: Cryptographic Architecture & Standards
        CardPanel cryptoCard = new CardPanel(new BorderLayout(0, 12), 16);
        JLabel cryptoTitle = new JLabel("CRYPTOGRAPHIC ARCHITECTURE SPECIFICATION");
        cryptoTitle.setFont(ThemeFonts.SECTION);
        cryptoTitle.setForeground(ThemeColors.PRIMARY_HOVER);
        cryptoCard.add(cryptoTitle, BorderLayout.NORTH);

        JEditorPane cryptoPane = new JEditorPane();
        cryptoPane.setContentType("text/html");
        cryptoPane.setEditable(false);
        cryptoPane.setOpaque(false);
        cryptoPane.setText("<html><body style='font-family: Segoe UI, sans-serif; font-size: 11pt; color: #cbd5e1;'>" +
                "<table cellpadding='4' cellspacing='0' style='width: 100%; border-collapse: collapse;'>" +
                "<tr><td style='color: #94a3b8;'><b>Cipher:</b></td><td>AES-256 in GCM Mode (Authenticated)</td></tr>" +
                "<tr><td style='color: #94a3b8;'><b>Tag Length:</b></td><td>128 bits (Tamper-evident verification)</td></tr>" +
                "<tr><td style='color: #94a3b8;'><b>Key Derivation:</b></td><td>PBKDF2WithHmacSHA256 (310,000 rounds)</td></tr>" +
                "<tr><td style='color: #94a3b8;'><b>Salt:</b></td><td>16 bytes (Fresh cryptographically secure random)</td></tr>" +
                "<tr><td style='color: #94a3b8;'><b>Nonce (IV):</b></td><td>12 bytes (Generated per encryption session)</td></tr>" +
                "<tr><td style='color: #94a3b8;'><b>Digest Engine:</b></td><td>SHA-256 Fingerprinting (FIPS 180-4)</td></tr>" +
                "<tr><td style='color: #94a3b8;'><b>File Format:</b></td><td>.senc binary container (Magic: 'CSHD')</td></tr>" +
                "<tr><td style='color: #94a3b8;'><b>Target Runtime:</b></td><td>Java SE 8+ (Standard library only)</td></tr>" +
                "</table>" +
                "<br/>" +
                "<div style='background-color: #1e293b; padding: 10px; border-radius: 6px; font-size: 10pt; color: #f1f5f9;'>" +
                "<b>Security Invariant:</b> No plaintext is ever written to disk or shown to the user unless the AES-GCM 128-bit authentication tag verifies successfully. Forgotten passwords cannot be decrypted." +
                "</div>" +
                "</body></html>");
        cryptoCard.add(cryptoPane, BorderLayout.CENTER);
        centerGrid.add(cryptoCard);

        add(centerGrid, BorderLayout.CENTER);

        // Footer
        CardPanel footer = new CardPanel(new FlowLayout(FlowLayout.CENTER, 16, 6), 6);
        JLabel ftLabel = new JLabel("CipherShield © 2026 • Designed for Academic Excellence & Practical Software Security");
        ftLabel.setFont(ThemeFonts.SMALL);
        ftLabel.setForeground(ThemeColors.TEXT_MUTED);
        footer.add(ftLabel);
        add(footer, BorderLayout.SOUTH);
    }
}
