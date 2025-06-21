package snake;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import snake.model.SnakeModel;
import snake.util.Constants;
import snake.util.GameConfig;
import snake.SaveData;
import snake.SaveData.SaveMetadata;
import snake.view.StyledButton;
import snake.sound.SoundManager;
import snake.util.EventManager;
import snake.util.GameEvent;

public class MainMenu {
    private static SnakeGameWindow currentWindow = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            initializeSound();
            createAndShowGUI();
        });
    }

    private static void initializeSound() {
        SoundManager.getInstance().loadSounds();
        EventManager eventManager = EventManager.getInstance();
        eventManager.addEventListener(GameEvent.EventType.FOOD_EATEN, SoundManager.getInstance());
        eventManager.addEventListener(GameEvent.EventType.GAME_OVER, SoundManager.getInstance());
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Snake Main Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Constants.COLOR_THEME_BG);

        JPanel panel = new JPanel();
        panel.setBackground(Constants.COLOR_THEME_BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // 标题
        JLabel titleLabel = new JLabel("Snake Game");
        titleLabel.setFont(Constants.FONT_UI_TITLE);
        titleLabel.setForeground(Constants.COLOR_THEME_TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(40));

        JButton btnNew      = new StyledButton("New Game");
        JButton btnContinue = new StyledButton("Continue Game");
        JButton btnLoad     = new StyledButton("Load Game");
        JButton btnSettings = new StyledButton("Settings");
        JButton btnQuit     = new StyledButton("Quit Game");

        btnNew.addActionListener(e -> { SaveData.delete(); openSizeDialog(frame); });
        btnContinue.addActionListener(e -> handleContinue());
        btnLoad.addActionListener(e -> openLoadDialog(frame));
        btnSettings.addActionListener(e -> openSettingsDialog(frame));
        btnQuit.addActionListener(e -> System.exit(0));

        // 统一设置按钮大小
        Dimension btnSize = new Dimension(220, 45);
        for(JButton btn : new JButton[]{btnNew, btnContinue, btnLoad, btnSettings, btnQuit}) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMinimumSize(btnSize);
            btn.setPreferredSize(btnSize);
            btn.setMaximumSize(btnSize);
            panel.add(btn);
            panel.add(Box.createVerticalStrut(15));
        }

        // 显示历史最高分
        panel.add(Box.createVerticalStrut(20));
        JLabel highScoreLabel = new JLabel("High Score: " + getHighScore());
        highScoreLabel.setFont(Constants.FONT_UI_DEFAULT);
        highScoreLabel.setForeground(Constants.COLOR_THEME_TEXT);
        highScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(highScoreLabel);

        frame.setContentPane(panel);
        frame.setVisible(true);

        frame.addWindowFocusListener(new WindowAdapter() {
            @Override public void windowGainedFocus(WindowEvent e) { 
                updateContinueEnabled(btnContinue);
                highScoreLabel.setText("High Score: " + getHighScore());
            }
        });
        updateContinueEnabled(btnContinue);
    }

    /* ---------- Continue Game ---------- */
    private static void handleContinue() {
        if (currentWindow != null && currentWindow.isRunning()) {
            currentWindow.bringToFront();
            return;
        }
        SnakeModel m = SaveData.read();
        if (m != null) currentWindow = new SnakeGameWindow(m);
    }

    private static void updateContinueEnabled(JButton btn) {
        boolean enable = (currentWindow != null && currentWindow.isRunning()) || SaveData.exists();
        btn.setEnabled(enable);
    }

    /* ---------- Load Game Dialog ---------- */
    private static void openLoadDialog(JFrame owner) {
        List<SaveMetadata> saves = SaveData.getAllSaves();
        if (saves.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "No saved games found.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dlg = new JDialog(owner, "Load Game", true);
        dlg.setSize(400, 350);
        dlg.setLocationRelativeTo(owner);
        dlg.getContentPane().setBackground(Constants.COLOR_THEME_PANEL);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Constants.COLOR_THEME_PANEL);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Select a save slot:");
        titleLabel.setFont(Constants.FONT_UI_DEFAULT);
        titleLabel.setForeground(Constants.COLOR_THEME_TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));

        for (int i = 0; i < Constants.MAX_SAVE_SLOTS; i++) {
            SaveMetadata metadata = SaveData.getMetadata(i);
            JButton slotButton;
            
            if (metadata != null) {
                slotButton = new StyledButton(metadata.toString());
                final int slot = i;
                slotButton.addActionListener(e -> {
                    SnakeModel model = SaveData.load(slot);
                    if (model != null) {
                        if (currentWindow != null && currentWindow.isRunning()) {
                            currentWindow.dispose();
                        }
                        currentWindow = new SnakeGameWindow(model);
                        dlg.dispose();
                    }
                });
            } else {
                slotButton = new StyledButton("Empty Slot " + i);
                slotButton.setEnabled(false);
            }
            
            slotButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            Dimension btnSize = new Dimension(320, 40);
            slotButton.setMinimumSize(btnSize);
            slotButton.setPreferredSize(btnSize);
            slotButton.setMaximumSize(btnSize);
            
            panel.add(slotButton);
            panel.add(Box.createVerticalStrut(10));
        }

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    /* ---------- Settings Dialog ---------- */
    private static void openSettingsDialog(JFrame owner) {
        JDialog dlg = new JDialog(owner, "Settings", true);
        dlg.setSize(350, 400);
        dlg.setLocationRelativeTo(owner);
        dlg.getContentPane().setBackground(Constants.COLOR_THEME_BG);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Constants.COLOR_THEME_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GameConfig config = GameConfig.getInstance();

        // 游戏速度设置
        JLabel speedLabel = new JLabel("Game Speed:");
        speedLabel.setForeground(Constants.COLOR_THEME_TEXT);
        speedLabel.setFont(Constants.FONT_UI_DEFAULT);
        panel.add(speedLabel);
        panel.add(Box.createVerticalStrut(5));

        String[] speeds = {"Slow", "Normal", "Fast", "Extreme"};
        int[] speedValues = {Constants.SPEED_SLOW, Constants.SPEED_NORMAL, Constants.SPEED_FAST, Constants.SPEED_EXTREME};
        JComboBox<String> speedCombo = new JComboBox<>(speeds);
        speedCombo.setSelectedItem(getSpeedName(config.getGameSpeed()));
        speedCombo.addActionListener(e -> {
            int selectedIndex = speedCombo.getSelectedIndex();
            config.setGameSpeed(speedValues[selectedIndex]);
        });
        panel.add(speedCombo);
        panel.add(Box.createVerticalStrut(20));

        // 自动存档设置
        JCheckBox autoSaveCheck = new JCheckBox("Enable Auto Save", config.isAutoSaveEnabled());
        autoSaveCheck.setForeground(Constants.COLOR_THEME_TEXT);
        autoSaveCheck.setBackground(Constants.COLOR_THEME_BG);
        autoSaveCheck.setFont(Constants.FONT_UI_DEFAULT);
        autoSaveCheck.addActionListener(e -> config.setAutoSaveEnabled(autoSaveCheck.isSelected()));
        panel.add(autoSaveCheck);
        panel.add(Box.createVerticalStrut(10));

        // 音效设置
        JCheckBox soundCheck = new JCheckBox("Enable Sound", config.isSoundEnabled());
        soundCheck.setForeground(Constants.COLOR_THEME_TEXT);
        soundCheck.setBackground(Constants.COLOR_THEME_BG);
        soundCheck.setFont(Constants.FONT_UI_DEFAULT);
        soundCheck.addActionListener(e -> config.setSoundEnabled(soundCheck.isSelected()));
        panel.add(soundCheck);
        panel.add(Box.createVerticalStrut(20));

        JSlider volumeSlider = new JSlider(0, 100, (int) (config.getSoundVolume() * 100));
        volumeSlider.addChangeListener(e -> {
            float volume = volumeSlider.getValue() / 100.0f;
            SoundManager.getInstance().setVolume(volume);
        });
        panel.add(volumeSlider);
        panel.add(Box.createVerticalStrut(20));

        // 关闭按钮
        JButton closeButton = new StyledButton("Close");
        closeButton.addActionListener(e -> dlg.dispose());
        panel.add(closeButton);

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    private static String getSpeedName(int speed) {
        if (speed == Constants.SPEED_SLOW) return "Slow";
        if (speed == Constants.SPEED_NORMAL) return "Normal";
        if (speed == Constants.SPEED_FAST) return "Fast";
        if (speed == Constants.SPEED_EXTREME) return "Extreme";
        return "Normal";
    }

    private static int getHighScore() {
        // 从所有存档中获取最高分
        List<SaveMetadata> saves = SaveData.getAllSaves();
        int highScore = 0;
        for (SaveMetadata save : saves) {
            if (save.score > highScore) {
                highScore = save.score;
            }
        }
        return highScore;
    }

    /* ---------- 新建游戏 ---------- */
    private static void openSizeDialog(JFrame owner) {
        JDialog dlg = new JDialog(owner, "Select Size", true);
        dlg.setSize(350, 280);
        dlg.setLocationRelativeTo(owner);
        dlg.getContentPane().setBackground(Constants.COLOR_THEME_BG);
        dlg.setLayout(new BoxLayout(dlg.getContentPane(), BoxLayout.Y_AXIS));

        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBackground(Constants.COLOR_THEME_BG);
        innerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton small  = new StyledButton("Small " + Constants.WINDOW_SMALL + " x " + Constants.WINDOW_SMALL);
        JButton medium = new StyledButton("Medium " + Constants.WINDOW_MEDIUM + " x " + Constants.WINDOW_MEDIUM);
        JButton large  = new StyledButton("Large " + Constants.WINDOW_LARGE + " x " + Constants.WINDOW_LARGE);

        small.addActionListener(e -> { dlg.dispose(); openGame(Constants.WINDOW_SMALL, Constants.WINDOW_SMALL); });
        medium.addActionListener(e -> { dlg.dispose(); openGame(Constants.WINDOW_MEDIUM, Constants.WINDOW_MEDIUM); });
        large.addActionListener(e -> { dlg.dispose(); openGame(Constants.WINDOW_LARGE, Constants.WINDOW_LARGE); });

        Dimension btnSize = new Dimension(280, 45);
        for(JButton btn : new JButton[]{small, medium, large}) {
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMinimumSize(btnSize);
            btn.setPreferredSize(btnSize);
            btn.setMaximumSize(btnSize);
            innerPanel.add(btn);
            innerPanel.add(Box.createVerticalStrut(15));
        }
        
        dlg.add(innerPanel);
        dlg.setVisible(true);
    }

    private static void openGame(int w, int h) {
        if (currentWindow != null && currentWindow.isRunning()) currentWindow.dispose();
        currentWindow = new SnakeGameWindow(w, h);
    }
}