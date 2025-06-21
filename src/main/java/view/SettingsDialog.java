package snake.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.List;

import snake.model.SnakeModel;
import snake.controller.SnakeController;
import snake.SaveData;
import snake.SaveData.SaveMetadata;
import snake.util.Constants;
import snake.util.GameConfig;
import snake.view.StyledButton;

public class SettingsDialog extends JDialog {
    private final SnakeModel model;
    private final SnakeController controller;
    private final GameConfig config;
    
    public SettingsDialog(JFrame owner,
                          SnakeModel model,
                          SnakeController ctl) {
        super(owner, "Settings", true);              // 模态对话框
        this.model = model;
        this.controller = ctl;
        this.config = GameConfig.getInstance();
        
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Constants.COLOR_THEME_BG);

        createUI();
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                controller.resume();
            }
        });
    }
    
    private void createUI() {
        // 使用 GridBagLayout 以获得更好的对齐控制
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Constants.COLOR_THEME_BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 15, 0); // 组件间的垂直间距
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平填充
        gbc.weightx = 1.0; // 占据全部水平空间
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTH;

        // 标题
        JLabel titleLabel = new JLabel("Game Settings");
        titleLabel.setFont(Constants.FONT_UI_TITLE);
        titleLabel.setForeground(Constants.COLOR_THEME_TEXT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        // 游戏控制按钮
        gbc.gridy = 1;
        JPanel gamePanel = createGamePanel();
        mainPanel.add(gamePanel, gbc);

        // 设置面板
        gbc.gridy = 2;
        JPanel settingsPanel = createSettingsPanel();
        mainPanel.add(settingsPanel, gbc);

        // 存档管理面板
        gbc.gridy = 3;
        JPanel savePanel = createSavePanel();
        mainPanel.add(savePanel, gbc);

        // 添加一个弹簧，将所有内容向上推
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        mainPanel.add(Box.createVerticalGlue(), gbc);

        // 使用 JScrollPane 避免内容溢出
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Constants.COLOR_THEME_BG);
        setContentPane(scrollPane);
    }
    
    private JPanel createGamePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1, 5, 5));
        panel.setBackground(Constants.COLOR_THEME_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Constants.COLOR_THEME_TEXT), 
            "Game Control", 0, 0, 
            new Font("Segoe UI", Font.BOLD, 14), Constants.COLOR_THEME_TEXT));

        JButton btnContinue = new StyledButton("Resume Game");
        JButton btnRestart  = new StyledButton("Restart Game");
        JButton btnSaveQuit = new StyledButton("Save and Quit");

        btnContinue.addActionListener(e -> {
            setVisible(false);
            controller.resume();
        });

        btnRestart.addActionListener(e -> {
            model.reset();
            setVisible(false);
            controller.resume();
        });

        btnSaveQuit.addActionListener(e -> {
            SaveData.autoSave(model);
            getOwner().dispose();
        });

        panel.add(btnContinue);
        panel.add(btnRestart);
        panel.add(btnSaveQuit);
        
        return panel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout()); // 改为GridBagLayout
        panel.setBackground(Constants.COLOR_THEME_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Constants.COLOR_THEME_TEXT), 
            "Settings", 0, 0, 
            new Font("Segoe UI", Font.BOLD, 14), Constants.COLOR_THEME_TEXT));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // 游戏速度设置
        JLabel speedLabel = new JLabel("Game Speed:");
        speedLabel.setForeground(Constants.COLOR_THEME_TEXT);
        speedLabel.setFont(Constants.FONT_UI_DEFAULT);
        gbc.gridy = 0;
        panel.add(speedLabel, gbc);
        
        String[] speeds = {"Slow", "Normal", "Fast", "Extreme"};
        int[] speedValues = {Constants.SPEED_SLOW, Constants.SPEED_NORMAL, Constants.SPEED_FAST, Constants.SPEED_EXTREME};
        JComboBox<String> speedCombo = new JComboBox<>(speeds);
        speedCombo.setSelectedItem(getSpeedName(config.getGameSpeed()));
        speedCombo.addActionListener(e -> {
            int selectedIndex = speedCombo.getSelectedIndex();
            config.setGameSpeed(speedValues[selectedIndex]);
            controller.setSpeed(speedValues[selectedIndex]);
        });
        gbc.gridy = 1;
        panel.add(speedCombo, gbc);
        
        // 自动存档设置
        JCheckBox autoSaveCheck = new JCheckBox("Enable Auto Save", config.isAutoSaveEnabled());
        autoSaveCheck.setForeground(Constants.COLOR_THEME_TEXT);
        autoSaveCheck.setBackground(Constants.COLOR_THEME_BG);
        autoSaveCheck.setFont(Constants.FONT_UI_DEFAULT);
        autoSaveCheck.addActionListener(e -> config.setAutoSaveEnabled(autoSaveCheck.isSelected()));
        gbc.gridy = 2;
        panel.add(autoSaveCheck, gbc);

        // 音效设置
        JCheckBox soundCheck = new JCheckBox("Enable Sound", config.isSoundEnabled());
        soundCheck.setForeground(Constants.COLOR_THEME_TEXT);
        soundCheck.setBackground(Constants.COLOR_THEME_BG);
        soundCheck.setFont(Constants.FONT_UI_DEFAULT);
        soundCheck.addActionListener(e -> config.setSoundEnabled(soundCheck.isSelected()));
        gbc.gridy = 3;
        panel.add(soundCheck, gbc);
        
        return panel;
    }
    
    private JPanel createSavePanel() {
        JPanel panel = new JPanel(new GridBagLayout()); // 改为GridBagLayout
        panel.setBackground(Constants.COLOR_THEME_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Constants.COLOR_THEME_TEXT), 
            "Save Management", 0, 0, 
            new Font("Segoe UI", Font.BOLD, 14), Constants.COLOR_THEME_TEXT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // 当前游戏信息
        JLabel infoLabel = new JLabel(String.format("Current Score: %d | Length: %d", 
            model.getScore(), model.getBody().size()));
        infoLabel.setForeground(Constants.COLOR_THEME_TEXT);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 0;
        panel.add(infoLabel, gbc);

        // 存档按钮
        JButton saveButton = new StyledButton("Save Current Game");
        saveButton.addActionListener(e -> {
            String saveName = JOptionPane.showInputDialog(this, 
                "Enter save name:", "Save Game", JOptionPane.QUESTION_MESSAGE);
            if (saveName != null && !saveName.trim().isEmpty()) {
                SaveData.save(model, saveName.trim(), 0);
                JOptionPane.showMessageDialog(this, "Game saved successfully!", 
                    "Save", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        gbc.gridy = 1;
        panel.add(saveButton, gbc);

        // 显示存档列表
        List<SaveMetadata> saves = SaveData.getAllSaves();
        if (!saves.isEmpty()) {
            JLabel savesLabel = new JLabel("Saved Games:");
            savesLabel.setForeground(Constants.COLOR_THEME_TEXT);
            savesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            gbc.gridy = 2;
            gbc.insets = new Insets(10, 5, 5, 5); // Add extra top margin
            panel.add(savesLabel, gbc);

            gbc.insets = new Insets(0, 5, 2, 5); // Reset insets for save list
            int currentY = 3;
            for (SaveMetadata save : saves) {
                JLabel saveInfo = new JLabel("  " + save.toString());
                saveInfo.setForeground(Constants.COLOR_THEME_TEXT);
                saveInfo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                gbc.gridy = currentY++;
                panel.add(saveInfo, gbc);
            }
        }
        
        return panel;
    }
    
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Constants.COLOR_THEME_BG);
        button.setForeground(Constants.COLOR_THEME_TEXT);
        button.setFont(Constants.FONT_UI_DEFAULT);
        button.setBorder(BorderFactory.createLineBorder(Constants.COLOR_THEME_TEXT, 1));
        button.setFocusPainted(false);
        return button;
    }
    
    private String getSpeedName(int speed) {
        if (speed == Constants.SPEED_SLOW) return "Slow";
        if (speed == Constants.SPEED_NORMAL) return "Normal";
        if (speed == Constants.SPEED_FAST) return "Fast";
        if (speed == Constants.SPEED_EXTREME) return "Extreme";
        return "Normal";
    }
}
