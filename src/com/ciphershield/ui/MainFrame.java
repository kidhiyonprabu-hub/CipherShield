package com.ciphershield.ui;

import com.ciphershield.ui.components.NavItem;
import com.ciphershield.ui.panels.*;
import com.ciphershield.ui.theme.IconFactory;
import com.ciphershield.ui.theme.ThemeColors;
import com.ciphershield.ui.theme.ThemeFonts;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window frame.
 * Houses the sidebar navigation and page view stack (CardLayout).
 */
public class MainFrame extends JFrame {

    public static final String PAGE_DASHBOARD = "DASHBOARD";
    public static final String PAGE_TEXT = "TEXT_CRYPTO";
    public static final String PAGE_FILE = "FILE_CRYPTO";
    public static final String PAGE_PRIVACY = "PRIVACY_GUARD";
    public static final String PAGE_BATCH = "BATCH_CENTER";
    public static final String PAGE_INTEGRITY = "INTEGRITY_LAB";
    public static final String PAGE_HISTORY = "HISTORY";
    public static final String PAGE_ABOUT = "ABOUT";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private final Map<String, NavItem> navItems = new HashMap<>();

    private DashboardPanel dashboardPanel;
    private TextCryptoPanel textCryptoPanel;
    private FileCryptoPanel fileCryptoPanel;
    private PrivacyGuardPanel privacyGuardPanel;
    private BatchCenterPanel batchCenterPanel;
    private IntegrityLabPanel integrityLabPanel;
    private HistoryPanel historyPanel;
    private AboutPanel aboutPanel;

    private String currentPage = PAGE_DASHBOARD;

    public MainFrame() {
        com.ciphershield.CipherShieldApp.log("MainFrame constructor started.");
        setTitle("CipherShield – Secure Encryption and Decryption Tool");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int windowWidth = Math.min(1440, Math.max(Math.min(1040, screen.width), (int) (screen.width * 0.90)));
        int windowHeight = Math.min(900, Math.max(Math.min(680, screen.height), (int) (screen.height * 0.90)));
        setSize(windowWidth, windowHeight);
        setMinimumSize(new Dimension(Math.min(1040, windowWidth), Math.min(680, windowHeight)));
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeColors.BACKGROUND);
        setLayout(new BorderLayout());

        // Set application icon
        try {
            com.ciphershield.CipherShieldApp.log("Setting frame icon...");
            setIconImage(IconFactory.createLogoIcon(64).getImage());
        } catch (Throwable t) {
            com.ciphershield.CipherShieldApp.logError("Failed to set frame icon", t);
        }

        // 1. Sidebar Navigation (West)
        com.ciphershield.CipherShieldApp.log("Creating sidebar...");
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Content Stack (Center)
        com.ciphershield.CipherShieldApp.log("Initializing pages...");
        initPages();
        add(contentArea, BorderLayout.CENTER);

        // Show Dashboard first
        com.ciphershield.CipherShieldApp.log("Navigating to initial page (DASHBOARD)...");
        navigateTo(PAGE_DASHBOARD);
        com.ciphershield.CipherShieldApp.log("MainFrame constructor finished.");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(ThemeColors.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(244, 750));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeColors.BORDER));

        // Top Brand Header
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 18));
        brandBox.setOpaque(false);
        JLabel logo = new JLabel(IconFactory.createLogoIcon(38));
        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);

        JLabel brandTitle = new JLabel("CipherShield");
        brandTitle.setFont(ThemeFonts.SUBTITLE);
        brandTitle.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel brandSub = new JLabel("CYBERSECURITY");
        brandSub.setFont(ThemeFonts.SMALL);
        brandSub.setForeground(ThemeColors.SECONDARY);

        brandText.add(brandTitle);
        brandText.add(brandSub);
        brandBox.add(logo);
        brandBox.add(brandText);

        sidebar.add(brandBox, BorderLayout.NORTH);

        // Navigation Items List
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(10, 8, 10, 8));

        addNavItem(navList, PAGE_DASHBOARD, "Dashboard", IconFactory.createDashboardIcon(18, ThemeColors.TEXT_SECONDARY));
        navList.add(Box.createVerticalStrut(4));
        addNavSectionLabel(navList, "SECURITY TOOLS");
        addNavItem(navList, PAGE_TEXT, "Text Crypto", IconFactory.createTextIcon(18, ThemeColors.TEXT_SECONDARY));
        navList.add(Box.createVerticalStrut(4));
        addNavItem(navList, PAGE_FILE, "File Security (.senc)", IconFactory.createFileIcon(18, ThemeColors.TEXT_SECONDARY));
        navList.add(Box.createVerticalStrut(4));
        addNavItem(navList, PAGE_PRIVACY, "PrivacyGuard", IconFactory.createPrivacyIcon(18, ThemeColors.TEXT_SECONDARY));
        navList.add(Box.createVerticalStrut(4));
        addNavItem(navList, PAGE_BATCH, "Batch Center", IconFactory.createBatchIcon(18, ThemeColors.TEXT_SECONDARY));
        navList.add(Box.createVerticalStrut(4));
        addNavItem(navList, PAGE_INTEGRITY, "Integrity Lab", IconFactory.createIntegrityIcon(18, ThemeColors.TEXT_SECONDARY));
        navList.add(Box.createVerticalStrut(4));
        addNavSectionLabel(navList, "ACTIVITY");
        addNavItem(navList, PAGE_HISTORY, "Security History", IconFactory.createHistoryIcon(18, ThemeColors.TEXT_SECONDARY));
        navList.add(Box.createVerticalStrut(4));
        addNavItem(navList, PAGE_ABOUT, "About & Specs", IconFactory.createAboutIcon(18, ThemeColors.TEXT_SECONDARY));

        sidebar.add(navList, BorderLayout.CENTER);

        // Sidebar Footer Status
        JPanel footerBox = new JPanel(new BorderLayout());
        footerBox.setOpaque(false);
        footerBox.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel ver = new JLabel("v1.0.0 • Java 8 SE");
        ver.setFont(ThemeFonts.SMALL);
        ver.setForeground(ThemeColors.TEXT_MUTED);

        JLabel statusDot = new JLabel("● SECURE ENGINE");
        statusDot.setFont(ThemeFonts.SMALL);
        statusDot.setForeground(ThemeColors.SUCCESS);

        footerBox.add(ver, BorderLayout.NORTH);
        footerBox.add(statusDot, BorderLayout.SOUTH);
        sidebar.add(footerBox, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel container, final String pageId, String title, Icon icon) {
        NavItem item = new NavItem(pageId, title, icon);
        item.setClickAction(new Runnable() {
            @Override
            public void run() {
                navigateTo(pageId);
            }
        });
        navItems.put(pageId, item);
        container.add(item);
    }

    private void addNavSectionLabel(JPanel container, String title) {
        JLabel label = new JLabel(title);
        label.setFont(ThemeFonts.SMALL.deriveFont(Font.BOLD, 11f));
        label.setForeground(ThemeColors.TEXT_MUTED);
        label.setBorder(new EmptyBorder(12, 16, 6, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.add(label);
    }

    private void initPages() {
        contentArea.setOpaque(false);

        com.ciphershield.CipherShieldApp.log("Initializing DashboardPanel...");
        dashboardPanel = new DashboardPanel(new DashboardPanel.PageNavigator() {
            @Override
            public void navigate(String pageId) {
                navigateTo(pageId);
            }
        });

        com.ciphershield.CipherShieldApp.log("Initializing TextCryptoPanel...");
        textCryptoPanel = new TextCryptoPanel();

        com.ciphershield.CipherShieldApp.log("Initializing FileCryptoPanel...");
        fileCryptoPanel = new FileCryptoPanel();

        com.ciphershield.CipherShieldApp.log("Initializing PrivacyGuardPanel...");
        privacyGuardPanel = new PrivacyGuardPanel(new PrivacyGuardPanel.ScanTransferListener() {
            @Override
            public void onTransferToEncrypt(String text) {
                navigateTo(PAGE_TEXT);
            }
        });

        com.ciphershield.CipherShieldApp.log("Initializing BatchCenterPanel...");
        batchCenterPanel = new BatchCenterPanel();

        com.ciphershield.CipherShieldApp.log("Initializing IntegrityLabPanel...");
        integrityLabPanel = new IntegrityLabPanel();

        com.ciphershield.CipherShieldApp.log("Initializing HistoryPanel...");
        historyPanel = new HistoryPanel();

        com.ciphershield.CipherShieldApp.log("Initializing AboutPanel...");
        aboutPanel = new AboutPanel();

        com.ciphershield.CipherShieldApp.log("Adding panels to CardLayout contentArea...");
        contentArea.add(dashboardPanel, PAGE_DASHBOARD);
        contentArea.add(textCryptoPanel, PAGE_TEXT);
        contentArea.add(fileCryptoPanel, PAGE_FILE);
        contentArea.add(privacyGuardPanel, PAGE_PRIVACY);
        contentArea.add(batchCenterPanel, PAGE_BATCH);
        contentArea.add(integrityLabPanel, PAGE_INTEGRITY);
        contentArea.add(historyPanel, PAGE_HISTORY);
        contentArea.add(aboutPanel, PAGE_ABOUT);
        com.ciphershield.CipherShieldApp.log("All pages initialized and added.");
    }

    public void navigateTo(String pageId) {
        this.currentPage = pageId;
        cardLayout.show(contentArea, pageId);

        // Update active nav indicators
        for (Map.Entry<String, NavItem> e : navItems.entrySet()) {
            e.getValue().setActive(e.getKey().equals(pageId));
        }

        // Refresh dynamic pages upon entry
        if (PAGE_DASHBOARD.equals(pageId) && dashboardPanel != null) {
            dashboardPanel.refreshData();
        } else if (PAGE_HISTORY.equals(pageId) && historyPanel != null) {
            historyPanel.loadHistoryData();
        }
    }
}
