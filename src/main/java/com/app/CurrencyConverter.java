package com.app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.server.*;

import com.server.AuthService;
import com.server.Database;
import com.server.RegistrationStatus;

import com.chart.CurrencyFetcher;

public class CurrencyConverter extends JFrame {

    // Enhanced currency data with flags and full names
    private static final Map<String, CurrencyData> CURRENCIES = new LinkedHashMap<>();
    static {
        CURRENCIES.put("USD", new CurrencyData("🇺🇸", "United States Dollar", "$"));
        CURRENCIES.put("EUR", new CurrencyData("🇪🇺", "Euro", "€"));
        CURRENCIES.put("GBP", new CurrencyData("🇬🇧", "British Pound Sterling", "£"));
        CURRENCIES.put("JPY", new CurrencyData("🇯🇵", "Japanese Yen", "¥"));
        CURRENCIES.put("AUD", new CurrencyData("🇦🇺", "Australian Dollar", "A$"));
        CURRENCIES.put("CAD", new CurrencyData("🇨🇦", "Canadian Dollar", "C$"));
        CURRENCIES.put("CHF", new CurrencyData("🇨🇭", "Swiss Franc", "Fr"));
        CURRENCIES.put("CNY", new CurrencyData("🇨🇳", "Chinese Yuan", "¥"));
        CURRENCIES.put("SEK", new CurrencyData("🇸🇪", "Swedish Krona", "kr"));
        CURRENCIES.put("NZD", new CurrencyData("🇳🇿", "New Zealand Dollar", "NZ$"));
        CURRENCIES.put("INR", new CurrencyData("🇮🇳", "Indian Rupee", "₹"));
        CURRENCIES.put("BRL", new CurrencyData("🇧🇷", "Brazilian Real", "R$"));
        CURRENCIES.put("RUB", new CurrencyData("🇷🇺", "Russian Ruble", "₽"));
        CURRENCIES.put("KRW", new CurrencyData("🇰🇷", "South Korean Won", "₩"));
        CURRENCIES.put("SGD", new CurrencyData("🇸🇬", "Singapore Dollar", "S$"));
    }

    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(99, 102, 241);
    private static final Color PRIMARY_DARK = new Color(79, 70, 229);
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color WARNING_COLOR = new Color(245, 158, 11);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    private static final Color TEXT_PRIMARY = new Color(17, 24, 39);
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    private static final Color SIDEBAR_COLOR = new Color(15, 23, 42);

    // Application state
    private String currentUser = null;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private boolean isLoggedIn = false;
    private javax.swing.Timer chartUpdateTimer;
    private JPanel chartArea;
    private String currentFromCurrency = "USD";
    private String currentToCurrency = "INR";

    // Simple user storage (in real app, use database)

    // Chart data for animation
    private List<Double> chartData = new ArrayList<>();
    private int chartAnimationFrame = 0;

    // Currency data class
    private static class CurrencyData {
        final String flag;
        final String name;
        final String symbol;

        CurrencyData(String flag, String name, String symbol) {
            this.flag = flag;
            this.name = name;
            this.symbol = symbol;
        }
    }

    public CurrencyConverter() {
        initializeApp();
        initializeChartData(currentFromCurrency, currentToCurrency);
    }

    private void initializeApp() {
        setTitle("Currency Converter Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setResizable(true);

        // Setup card layout for page switching
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Create pages
        mainContainer.add(createLoginPage(), "LOGIN");
        mainContainer.add(createRegisterPage(), "REGISTER");
        mainContainer.add(createDashboardPage(), "DASHBOARD");

        add(mainContainer);

        // Start with login page
        cardLayout.show(mainContainer, "LOGIN");
    }

    private void initializeChartData(String base, String target) {
        List<Double> fetchedRates = CurrencyFetcher.fetchCurrencyTrend(base, target, 30);
        chartData.clear();

        if (fetchedRates.isEmpty()) {
            // fallback to random data
            for (int i = 0; i < 30; i++) {
                chartData.add(83.0 + Math.random() * 2 - 1);
            }
        } else {
            chartData.addAll(fetchedRates);
        }

    }

    private void updateChartData(String base, String target) {
        List<Double> rates = CurrencyFetcher.fetchCurrencyTrend(base, target, 30);
        chartData.clear();
        if (rates.isEmpty()) {
            // fallback random
            for (int i = 0; i < 30; i++)
                chartData.add(83.0 + Math.random() * 2 - 1);
        } else {
            chartData.addAll(rates);
        }
        if (chartArea != null)
            chartArea.repaint();
    }

    private JPanel createLoginPage() {
        JPanel loginPage = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(67, 56, 202),
                        getWidth(), getHeight(), new Color(147, 51, 234));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        loginPage.setLayout(new BorderLayout());

        // Main content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        // Login card
        JPanel loginCard = createRoundedPanel(CARD_COLOR, 20);
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBorder(new EmptyBorder(40, 40, 40, 40));
        loginCard.setPreferredSize(new Dimension(400, 500));

        // Logo and title
        JLabel logoLabel = new JLabel("💱", SwingConstants.CENTER);
        logoLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Currency Converter Pro", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in to your account", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form fields with proper placeholders
        JTextField usernameField = createPlaceholderTextField("Enter your username");
        JPasswordField passwordField = createPlaceholderPasswordField("Enter your password");

        // Buttons
        JButton loginButton = createStyledButton("Sign In", PRIMARY_COLOR, Color.WHITE);
        JButton registerButton = createStyledButton("Create Account", Color.WHITE, PRIMARY_COLOR);
        registerButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                new EmptyBorder(12, 24, 12, 24)));

        // Event listeners
        loginButton.addActionListener(e -> handleLogin(usernameField.getText(),
                new String(passwordField.getPassword())));
        registerButton.addActionListener(e -> cardLayout.show(mainContainer, "REGISTER"));

        // Enter key support
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin(usernameField.getText(), new String(passwordField.getPassword()));
                }
            }
        };
        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);

        // Layout
        loginCard.add(logoLabel);
        loginCard.add(Box.createVerticalStrut(10));
        loginCard.add(titleLabel);
        loginCard.add(Box.createVerticalStrut(5));
        loginCard.add(subtitleLabel);
        loginCard.add(Box.createVerticalStrut(30));
        loginCard.add(usernameField);
        loginCard.add(Box.createVerticalStrut(15));
        loginCard.add(passwordField);
        loginCard.add(Box.createVerticalStrut(20));
        loginCard.add(loginButton);
        loginCard.add(Box.createVerticalStrut(10));
        loginCard.add(registerButton);

        centerPanel.add(loginCard);

        // Footer
        JPanel footerPanel = createFooterPanel(true);

        loginPage.add(centerPanel, BorderLayout.CENTER);
        loginPage.add(footerPanel, BorderLayout.SOUTH);

        return loginPage;
    }

    private JPanel createRegisterPage() {
        JPanel registerPage = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Gradient background
                GradientPaint gradient = new GradientPaint(0, 0, new Color(16, 185, 129),
                        getWidth(), getHeight(), new Color(5, 150, 105));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        registerPage.setLayout(new BorderLayout());

        // Main content
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        // Register card
        JPanel registerCard = createRoundedPanel(CARD_COLOR, 20);
        registerCard.setLayout(new BoxLayout(registerCard, BoxLayout.Y_AXIS));
        registerCard.setBorder(new EmptyBorder(40, 40, 40, 40));
        registerCard.setPreferredSize(new Dimension(400, 550));

        // Logo and title
        JLabel logoLabel = new JLabel("🚀", SwingConstants.CENTER);
        logoLabel.setFont(new Font("SansSerif", Font.PLAIN, 48));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Join Currency Converter Pro", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form fields with proper placeholders
        JTextField usernameField = createPlaceholderTextField("Choose a username");
        JPasswordField passwordField = createPlaceholderPasswordField("Create a password");
        JPasswordField confirmPasswordField = createPlaceholderPasswordField("Confirm your password");

        // Buttons
        JButton registerButton = createStyledButton("Create Account", SUCCESS_COLOR, Color.WHITE);
        JButton backButton = createStyledButton("Back to Login", Color.WHITE, SUCCESS_COLOR);
        backButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
                new EmptyBorder(12, 24, 12, 24)));

        // Event listeners
        registerButton.addActionListener(e -> handleRegister(usernameField.getText(),
                new String(passwordField.getPassword()), new String(confirmPasswordField.getPassword())));
        backButton.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));

        // Layout
        registerCard.add(logoLabel);
        registerCard.add(Box.createVerticalStrut(10));
        registerCard.add(titleLabel);
        registerCard.add(Box.createVerticalStrut(5));
        registerCard.add(subtitleLabel);
        registerCard.add(Box.createVerticalStrut(30));
        registerCard.add(usernameField);
        registerCard.add(Box.createVerticalStrut(15));
        registerCard.add(passwordField);
        registerCard.add(Box.createVerticalStrut(15));
        registerCard.add(confirmPasswordField);
        registerCard.add(Box.createVerticalStrut(20));
        registerCard.add(registerButton);
        registerCard.add(Box.createVerticalStrut(10));
        registerCard.add(backButton);

        centerPanel.add(registerCard);

        // Footer
        JPanel footerPanel = createFooterPanel(true);

        registerPage.add(centerPanel, BorderLayout.CENTER);
        registerPage.add(footerPanel, BorderLayout.SOUTH);

        return registerPage;
    }

    private JPanel createDashboardPage() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(BACKGROUND_COLOR);

        // Create sidebar
        JPanel sidebar = createSidebar();

        // Create main content area
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BACKGROUND_COLOR);
        mainContent.setBorder(new EmptyBorder(20, 20, 10, 20));

        // Left side - Converter
        JPanel converterPanel = createConverterPanel();

        // Right side - Chart
        JPanel chartPanel = createChartPanel();

        // Split main content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, converterPanel, chartPanel);
        splitPane.setDividerLocation(650);
        splitPane.setBackground(BACKGROUND_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(10);

        mainContent.add(splitPane, BorderLayout.CENTER);

        dashboard.add(sidebar, BorderLayout.WEST);
        dashboard.add(mainContent, BorderLayout.CENTER);

        return dashboard;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        // Logo section - centered
        JPanel logoSection = new JPanel();
        logoSection.setBackground(SIDEBAR_COLOR);
        logoSection.setLayout(new BoxLayout(logoSection, BoxLayout.Y_AXIS));
        logoSection.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoIcon = new JLabel("💱", SwingConstants.CENTER);
        logoIcon.setFont(new Font("SansSerif", Font.PLAIN, 32));
        logoIcon.setForeground(Color.WHITE);
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoText = new JLabel("CurrencyPro", SwingConstants.CENTER);
        logoText.setFont(new Font("SansSerif", Font.BOLD, 20));
        logoText.setForeground(Color.WHITE);
        logoText.setAlignmentX(Component.CENTER_ALIGNMENT);

        logoSection.add(logoIcon);
        logoSection.add(Box.createVerticalStrut(5));
        logoSection.add(logoText);
        logoSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // User section - centered
        JPanel userSection = new JPanel();
        userSection.setBackground(SIDEBAR_COLOR);
        userSection.setLayout(new BoxLayout(userSection, BoxLayout.Y_AXIS));
        userSection.setBorder(new EmptyBorder(30, 10, 30, 10));
        userSection.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userIcon = new JLabel("👤", SwingConstants.CENTER);
        userIcon.setForeground(Color.WHITE);
        userIcon.setFont(new Font("SansSerif", Font.PLAIN, 24));
        userIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel(currentUser != null ? currentUser : "User", SwingConstants.CENTER);
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        userLabel.setForeground(Color.WHITE);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel("Online", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        userSection.add(userIcon);
        userSection.add(Box.createVerticalStrut(8));
        userSection.add(userLabel);
        userSection.add(Box.createVerticalStrut(3));
        userSection.add(statusLabel);
        userSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Navigation menu
        JPanel menuSection = new JPanel();
        menuSection.setBackground(SIDEBAR_COLOR);
        menuSection.setLayout(new BoxLayout(menuSection, BoxLayout.Y_AXIS));
        menuSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton converterBtn = createSidebarButton("💱 Converter", true);
        JButton historyBtn = createSidebarButton("📊 History", false);
        JButton settingsBtn = createSidebarButton("⚙️ Settings", false);

        menuSection.add(converterBtn);
        menuSection.add(Box.createVerticalStrut(8));
        menuSection.add(historyBtn);
        menuSection.add(Box.createVerticalStrut(8));
        menuSection.add(settingsBtn);
        menuSection.add(Box.createVerticalGlue());

        // Logout button at bottom
        JButton logoutBtn = createSidebarButton("🚪 Logout", false);
        logoutBtn.setBackground(ERROR_COLOR);
        logoutBtn.addActionListener(e -> {
            currentUser = null;
            isLoggedIn = false;
            if (chartUpdateTimer != null) {
                chartUpdateTimer.stop();
            }
            cardLayout.show(mainContainer, "LOGIN");
            showNotification("Logged out successfully", SUCCESS_COLOR);
        });
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logoSection);
        sidebar.add(userSection);
        sidebar.add(menuSection);
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton createSidebarButton(String text, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(isActive ? PRIMARY_COLOR : SIDEBAR_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(15, 20, 15, 20));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Rounded corners
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                new EmptyBorder(10, 15, 10, 15)));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isActive) {
                    button.setBackground(new Color(55, 65, 81));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!isActive) {
                    button.setBackground(SIDEBAR_COLOR);
                }
            }
        });

        return button;
    }

    private JPanel createConverterPanel() {
        JPanel panel = createRoundedPanel(CARD_COLOR, 15);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Header
        JLabel headerLabel = new JLabel("Convert & Monitor any currency!");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setForeground(TEXT_PRIMARY);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Amount section - FIXED: Compact and properly styled
        JPanel amountSection = new JPanel(new BorderLayout());
        amountSection.setBackground(CARD_COLOR);
        amountSection.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel amountLabel = new JLabel("Amount");
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        amountLabel.setForeground(TEXT_PRIMARY);

        JTextField amountField = createPlaceholderTextField("1000.00");
        amountField.setFont(new Font("SansSerif", Font.BOLD, 16)); // Increased font but compact size
        amountField.setPreferredSize(new Dimension(0, 65)); // Compact height
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        amountSection.add(amountLabel, BorderLayout.NORTH);
        amountSection.add(Box.createVerticalStrut(12));
        amountSection.add(amountField, BorderLayout.CENTER);

        // Currency selection - FIXED: Smart currency swapping
        JPanel currencySection = new JPanel(new GridLayout(1, 3, 20, 0));
        currencySection.setBackground(CARD_COLOR);
        currencySection.setBorder(new EmptyBorder(20, 0, 20, 0));

        String[] currencyCodes = CURRENCIES.keySet().toArray(new String[0]);
        JComboBox<String> fromCombo = createStyledComboBox(currencyCodes);
        JComboBox<String> toCombo = createStyledComboBox(currencyCodes);

        fromCombo.setSelectedIndex(0); // USD
        toCombo.setSelectedIndex(10); // INR

        JButton swapButton = createIconButton("⇄");

        // FIXED: Smart currency selection logic
        fromCombo.addActionListener(e -> {
            String selectedFrom = (String) fromCombo.getSelectedItem();
            String selectedTo = (String) toCombo.getSelectedItem();

            if (selectedFrom != null && selectedFrom.equals(selectedTo)) {
                // Find a different currency for 'to'
                for (int i = 0; i < currencyCodes.length; i++) {
                    if (!currencyCodes[i].equals(selectedFrom)) {
                        toCombo.setSelectedItem(currencyCodes[i]);
                        break;
                    }
                }
            }
            currentFromCurrency = selectedFrom;
            performConversion(amountField, fromCombo, toCombo, null, null);
        });

        toCombo.addActionListener(e -> {
            String selectedFrom = (String) fromCombo.getSelectedItem();
            String selectedTo = (String) toCombo.getSelectedItem();

            if (selectedTo != null && selectedTo.equals(selectedFrom)) {
                // Find a different currency for 'from'
                for (int i = 0; i < currencyCodes.length; i++) {
                    if (!currencyCodes[i].equals(selectedTo)) {
                        fromCombo.setSelectedItem(currencyCodes[i]);
                        break;
                    }
                }
            }
            performConversion(amountField, fromCombo, toCombo, null, null);
        });

        currencySection.add(fromCombo);
        currencySection.add(swapButton);
        currencySection.add(toCombo);

        // Convert button
        JButton convertButton = createStyledButton("Convert Currency", PRIMARY_COLOR, Color.WHITE);
        convertButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        convertButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Result panel
        JPanel resultPanel = createRoundedPanel(new Color(240, 253, 244), 10);
        resultPanel.setBorder(new EmptyBorder(20, 20, 50, 20));
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

        JLabel resultLabel = new JLabel("0.00", SwingConstants.CENTER);
        resultLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        resultLabel.setForeground(SUCCESS_COLOR);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel rateLabel = new JLabel("Exchange rate will appear here", SwingConstants.CENTER);
        rateLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rateLabel.setForeground(TEXT_SECONDARY);
        rateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultPanel.add(resultLabel);
        resultPanel.add(Box.createVerticalStrut(10));
        resultPanel.add(rateLabel);

        // swap action
        swapButton.addActionListener(e -> {
            int fromIndex = fromCombo.getSelectedIndex();
            int toIndex = toCombo.getSelectedIndex();
            fromCombo.setSelectedIndex(toIndex);
            toCombo.setSelectedIndex(fromIndex);
            performConversion(amountField, fromCombo, toCombo, resultLabel, rateLabel);

        });

        // Convert action
        convertButton.addActionListener(e -> {
            String selectedFrom = (String) fromCombo.getSelectedItem();
            String selectedTo = (String) toCombo.getSelectedItem();

            performConversion(amountField, fromCombo, toCombo, resultLabel, rateLabel);
            updateChartData(selectedFrom, selectedTo);
        });

        // Footer
        JPanel footerPanel = createFooterPanel(false);
        // Layout
        panel.add(headerLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(amountSection);
        panel.add(currencySection);
        panel.add(Box.createVerticalStrut(20));
        panel.add(convertButton);
        panel.add(Box.createVerticalStrut(60));
        panel.add(resultPanel);
        panel.add(Box.createVerticalStrut(40));
        panel.add(footerPanel, BorderLayout.SOUTH);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createChartPanel() {
        JPanel panel = createRoundedPanel(CARD_COLOR, 15);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));
        panel.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(CARD_COLOR);

        JLabel headerLabel = new JLabel("Live Exchange Rate");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Real-time market data");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);

        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // FIXED: Professional animated chart
        chartArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                drawProfessionalChart(g2d, getWidth(), getHeight());
            }
        };
        chartArea.setBackground(CARD_COLOR);

        // Stats panel - FIXED: Professional market stats
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        statsPanel.setBackground(CARD_COLOR);
        statsPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // Dynamic stats that update
        statsPanel
                .add(createStatCard("Current Rate", String.format("%.4f", getCurrentRate()), "↑ 0.15%", SUCCESS_COLOR));
        statsPanel.add(createStatCard("24h High", String.format("%.4f", getCurrentRate() * 1.02), "Peak today",
                WARNING_COLOR));
        statsPanel.add(
                createStatCard("24h Low", String.format("%.4f", getCurrentRate() * 0.98), "Low today", ERROR_COLOR));
        statsPanel.add(createStatCard("Volume", "2.5M", "Transactions", PRIMARY_COLOR));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(chartArea, BorderLayout.CENTER);
        panel.add(statsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private double getCurrentRate() {
        return chartData.isEmpty() ? 83.25 : chartData.get(chartData.size() - 1);
    }

    private void drawProfessionalChart(Graphics2D g2d, int width, int height) {
        if (chartData.isEmpty())
            return;

        int margin = 40;
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin - 60;

        // Background gradient
        GradientPaint bgGradient = new GradientPaint(0, 0, new Color(248, 250, 252),
                0, height, Color.WHITE);
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, width, height);

        // Grid lines
        g2d.setColor(new Color(241, 245, 249));
        g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 0; i <= 8; i++) {
            int y = margin + (chartHeight * i / 8);
            g2d.drawLine(margin, y, margin + chartWidth, y);
        }

        for (int i = 0; i <= 10; i++) {
            int x = margin + (chartWidth * i / 10);
            g2d.drawLine(x, margin, x, margin + chartHeight);
        }

        if (chartData.size() < 2)
            return;

        // Calculate bounds
        double minRate = chartData.stream().mapToDouble(d -> d).min().orElse(0);
        double maxRate = chartData.stream().mapToDouble(d -> d).max().orElse(0);
        double range = maxRate - minRate;
        if (range == 0)
            range = 1;

        // Draw area fill
        int[] xPoints = new int[chartData.size() + 2];
        int[] yPoints = new int[chartData.size() + 2];

        for (int i = 0; i < chartData.size(); i++) {
            xPoints[i] = margin + (chartWidth * i / (chartData.size() - 1));
            yPoints[i] = margin + chartHeight - (int) ((chartData.get(i) - minRate) / range * chartHeight);
        }

        // Close the polygon for area fill
        xPoints[chartData.size()] = xPoints[chartData.size() - 1];
        yPoints[chartData.size()] = margin + chartHeight;
        xPoints[chartData.size() + 1] = xPoints[0];
        yPoints[chartData.size() + 1] = margin + chartHeight;

        // Area gradient
        GradientPaint areaGradient = new GradientPaint(0, margin,
                new Color(99, 102, 241, 80), 0, margin + chartHeight,
                new Color(99, 102, 241, 10));
        g2d.setPaint(areaGradient);
        g2d.fillPolygon(xPoints, yPoints, chartData.size() + 2);

        // Draw main line with gradient effect
        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 0; i < chartData.size() - 1; i++) {
            // Gradient line effect
            GradientPaint lineGradient = new GradientPaint(
                    xPoints[i], yPoints[i], PRIMARY_COLOR,
                    xPoints[i + 1], yPoints[i + 1], PRIMARY_DARK);
            g2d.setPaint(lineGradient);
            g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }

        // Draw data points with glow effect
        for (int i = 0; i < chartData.size(); i++) {
            // Glow effect
            for (int radius = 8; radius > 0; radius--) {
                int alpha = 255 - (radius * 30);
                g2d.setColor(new Color(99, 102, 241, Math.max(0, alpha)));
                g2d.fillOval(xPoints[i] - radius, yPoints[i] - radius, radius * 2, radius * 2);
            }

            // Main point
            g2d.setColor(Color.WHITE);
            g2d.fillOval(xPoints[i] - 4, yPoints[i] - 4, 8, 8);
            g2d.setColor(PRIMARY_COLOR);
            g2d.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
        }

        // Draw value labels
        g2d.setColor(TEXT_SECONDARY);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        DecimalFormat df = new DecimalFormat("#0.000");

        // Y-axis labels
        for (int i = 0; i <= 4; i++) {
            double value = minRate + (range * i / 4);
            int y = margin + chartHeight - (chartHeight * i / 4);
            g2d.drawString(df.format(value), 5, y + 4);
        }

        // Current value highlight
        if (!chartData.isEmpty()) {
            double currentValue = chartData.get(chartData.size() - 1);
            String valueStr = df.format(currentValue);

            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(valueStr);

            int lastX = xPoints[chartData.size() - 1];
            int lastY = yPoints[chartData.size() - 1];

            // Value label background
            g2d.setColor(PRIMARY_COLOR);
            g2d.fillRoundRect(lastX - labelWidth / 2 - 8, lastY - 25, labelWidth + 16, 20, 10, 10);

            // Value label text
            g2d.setColor(Color.WHITE);
            g2d.drawString(valueStr, lastX - labelWidth / 2, lastY - 10);
        }
    }

    private JPanel createStatCard(String title, String value, String change, Color color) {
        JPanel card = createRoundedPanel(new Color(248, 250, 252), 8);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        changeLabel.setForeground(color);
        changeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(changeLabel);

        return card;
    }

    private JPanel createFooterPanel(boolean isWhite) {
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBorder(new EmptyBorder(10, 20, 15, 20));

        JLabel line1 = new JLabel("This project was developed as a mini project for OOPM Java Lab",
                SwingConstants.CENTER);
        line1.setFont(new Font("SansSerif", Font.PLAIN, 13));
        line1.setForeground(isWhite ? Color.WHITE : Color.DARK_GRAY);
        line1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel line2 = new JLabel("by SY ECS1 students: Anvay, Sayali, Shashank & Ayushi", SwingConstants.CENTER);
        line2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        line2.setForeground(isWhite ? Color.WHITE : Color.DARK_GRAY);
        line2.setAlignmentX(Component.CENTER_ALIGNMENT);

        footerPanel.add(line1);
        footerPanel.add(Box.createVerticalStrut(3));
        footerPanel.add(line2);

        return footerPanel;
    }

    private void performConversion(JTextField amountField, JComboBox<String> fromCombo,
            JComboBox<String> toCombo, JLabel resultLabel, JLabel rateLabel) {
        try {
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty() || amountText.equals("Enter amount")) {
                return;
            }

            double amount = Double.parseDouble(amountText);
            String from = (String) fromCombo.getSelectedItem();
            String to = (String) toCombo.getSelectedItem();

            if (amount <= 0) {
                showNotification("Amount must be greater than 0", ERROR_COLOR);
                return;
            }

            // Get exchange rate
            SwingWorker<Double, Void> worker = new SwingWorker<Double, Void>() {
                @Override
                protected Double doInBackground() throws Exception {
                    Thread.sleep(500); // Simulate API delay
                    return getExchangeRate(from, to);
                }

                @Override
                protected void done() {
                    try {
                        double rate = get();
                        double result = amount * rate;

                        if (resultLabel != null && rateLabel != null) {
                            DecimalFormat formatter = new DecimalFormat("#,##0.00");
                            resultLabel.setText(formatter.format(result));
                            rateLabel.setText(String.format("1 %s = %.4f %s", from, rate, to));
                            String symbol = CURRENCIES.get(to).symbol;
                            // Animate result
                            animateValue(resultLabel, 0, result, 800, symbol);
                        }

                    } catch (Exception ex) {
                        if (resultLabel != null && rateLabel != null) {
                            resultLabel.setText("Error");
                            rateLabel.setText("Failed to fetch exchange rate");
                        }
                        showNotification("Conversion failed", ERROR_COLOR);
                    }
                }
            };

            worker.execute();

        } catch (NumberFormatException e) {
            showNotification("Please enter a valid number", ERROR_COLOR);
        }
    }

    private void animateValue(JLabel label, double start, double end, int duration, String symbol) {
        javax.swing.Timer timer = new javax.swing.Timer(20, null);
        long startTime = System.currentTimeMillis();
        DecimalFormat formatter = new DecimalFormat("#,##0.00");

        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            double progress = Math.min(1.0, (double) elapsed / duration);

            // Easing function
            progress = 1 - Math.pow(1 - progress, 3);

            double current = start + (end - start) * progress;
            label.setText(symbol + " " + formatter.format(current));

            if (progress >= 1.0) {
                timer.stop();
            }
        });

        timer.start();
    }

    private double getExchangeRate(String from, String to) throws IOException {
        if (from.equals(to))
            return 1.0;

        try {
            String apiUrl = "https://api.exchangerate-api.com/v4/latest/" + from;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != 200) {
                throw new IOException("API request failed");
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonObject rates = jsonObject.getAsJsonObject("rates");

            if (!rates.has(to)) {
                throw new IOException("Currency not supported: " + to);
            }

            return rates.get(to).getAsDouble();

        } catch (Exception e) {
            return getMockExchangeRate(from, to);
        }
    }

    private double getMockExchangeRate(String from, String to) {
        Map<String, Double> usdRates = new HashMap<>();
        usdRates.put("EUR", 0.85);
        usdRates.put("GBP", 0.73);
        usdRates.put("JPY", 110.0);
        usdRates.put("INR", 83.25);
        usdRates.put("CAD", 1.25);
        usdRates.put("AUD", 1.35);
        usdRates.put("CHF", 0.92);
        usdRates.put("CNY", 6.45);
        usdRates.put("SEK", 8.75);
        usdRates.put("NZD", 1.42);
        usdRates.put("BRL", 5.2);
        usdRates.put("RUB", 74.5);
        usdRates.put("KRW", 1180.0);
        usdRates.put("SGD", 1.35);

        if (from.equals("USD")) {
            return usdRates.getOrDefault(to, 1.0);
        } else if (to.equals("USD")) {
            return 1.0 / usdRates.getOrDefault(from, 1.0);
        } else {
            double fromToUsd = 1.0 / usdRates.getOrDefault(from, 1.0);
            double usdToTo = usdRates.getOrDefault(to, 1.0);
            return fromToUsd * usdToTo;
        }
    }

    private void handleLogin(String username, String password) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showNotification("Please fill in all fields", ERROR_COLOR);
            return;
        }

        if (AuthService.loginUser(username, password)) {
            currentUser = username;
            isLoggedIn = true;
            cardLayout.show(mainContainer, "DASHBOARD");
            showNotification("Welcome back, " + username + "!", SUCCESS_COLOR);
        } else {
            showNotification("Invalid username or password", ERROR_COLOR);
        }
    }

    private void handleRegister(String username, String password, String confirmPassword) {
        if (username.trim().isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            showNotification("Please fill in all fields", ERROR_COLOR);
            return;
        }

        if (password.length() < 6) {
            showNotification("Password must be at least 6 characters", ERROR_COLOR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showNotification("Passwords do not match", ERROR_COLOR);
            return;
        }

        RegistrationStatus status = AuthService.registerUser(username, password);
        if (status == RegistrationStatus.USER_ALREADY_EXISTS) {
            showNotification("Username already exists", WARNING_COLOR);
            return;
        } else if (status == RegistrationStatus.FAILURE) {
            showNotification("Registration failed. Please try again.", ERROR_COLOR);
            return;
        }

        showNotification("Account created successfully! Please login.", SUCCESS_COLOR);
        cardLayout.show(mainContainer, "LOGIN");
    }

    private void showNotification(String message, Color color) {
        JWindow notification = new JWindow(this);
        notification.setSize(350, 70);
        notification.setLocationRelativeTo(this);
        notification.setAlwaysOnTop(true);

        JPanel panel = createRoundedPanel(color, 10);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(Color.WHITE);

        panel.add(label);
        notification.add(panel);
        notification.setVisible(true);

        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> {
            notification.setVisible(false);
            notification.dispose();
        });
        timer.setRepeats(false);
        timer.start();

        // Slide animation
        javax.swing.Timer slideTimer = new javax.swing.Timer(10, null);
        int[] y = { -70 };
        slideTimer.addActionListener(e -> {
            y[0] += 4;
            if (y[0] >= 20) {
                y[0] = 20;
                slideTimer.stop();
            }
            notification.setLocation(notification.getX(), y[0]);
        });
        slideTimer.start();
    }

    private JTextField createPlaceholderTextField(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(12, 16, 12, 16)));
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(0, 45));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Set initial placeholder
        field.setText(placeholder);
        field.setForeground(TEXT_SECONDARY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        new EmptyBorder(12, 16, 12, 16)));
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 2),
                        new EmptyBorder(12, 16, 12, 16)));
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_SECONDARY);
                }
            }
        });

        return field;
    }

    private JPasswordField createPlaceholderPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(12, 16, 12, 16)));
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(0, 45));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Custom placeholder for password field
        field.setEchoChar((char) 0); // Show text initially
        field.setText(placeholder);
        field.setForeground(TEXT_SECONDARY);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        new EmptyBorder(12, 16, 12, 16)));
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('•'); // Show dots for password
                    field.setForeground(TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 2),
                        new EmptyBorder(12, 16, 12, 16)));
                if (String.valueOf(field.getPassword()).trim().isEmpty()) {
                    field.setEchoChar((char) 0); // Show placeholder text
                    field.setText(placeholder);
                    field.setForeground(TEXT_SECONDARY);
                }
            }
        });

        return field;
    }

    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        Color originalBg = bgColor;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(originalBg.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
            }
        });

        return button;
    }

    private JButton createIconButton(String icon) {
        JButton button = new JButton(icon);
        button.setFont(new Font("SansSerif", Font.BOLD, 20));
        button.setForeground(PRIMARY_COLOR);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                new EmptyBorder(10, 15, 10, 15)));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(60, 45));
        button.setMaximumSize(new Dimension(60, 45));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setForeground(PRIMARY_COLOR);
            }
        });

        return button;
    }

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(0, 45));
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        combo.setRenderer(new CurrencyComboRenderer());
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                new EmptyBorder(5, 10, 5, 10)));

        return combo;
    }

    private JPanel createRoundedPanel(Color backgroundColor, int cornerRadius) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(backgroundColor);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

                super.paintComponent(g);
            }

            @Override
            public void setOpaque(boolean isOpaque) {
                super.setOpaque(false);
            }
        };
    }

    private class CurrencyComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof String) {
                String code = (String) value;
                CurrencyData data = CURRENCIES.get(code);
                if (data != null) {
                    setText(data.flag + " " + code + " - " + data.name);
                }
            }

            setFont(new Font("SansSerif", Font.PLAIN, 13));
            setBorder(new EmptyBorder(8, 12, 8, 12));

            if (isSelected) {
                setBackground(PRIMARY_COLOR);
                setForeground(Color.WHITE);
            }

            return this;
        }
    }

    public static void main(String[] args) {
        Database.initialize();
        SwingUtilities.invokeLater(() -> {
            new CurrencyConverter().setVisible(true);
        });
    }
}