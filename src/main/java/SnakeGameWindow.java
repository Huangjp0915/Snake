package snake;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import snake.controller.SnakeController;
import snake.model.SnakeModel;
import snake.util.Constants;
import snake.util.GameState;
import snake.util.GameConfig;
import snake.util.EventManager;
import snake.util.GameEvent;
import snake.view.SnakePanel;
import snake.view.SettingsDialog;
import snake.view.GearIcon;
import snake.SaveData;

public class SnakeGameWindow extends JFrame {
    private final SnakeModel     model;
    private final SnakePanel     panel;
    private final SnakeController controller;
    private       SettingsDialog settings;
    private JButton gear;
    private boolean isFullScreen = false;
    private GraphicsDevice device;

    /* 新游戏 */
    public SnakeGameWindow(int width, int height) {
        this(new SnakeModel(width / Constants.UNIT,
                            height / Constants.UNIT));
    }

    /* 继续游戏 / 载入存档 */
    public SnakeGameWindow(SnakeModel m) {
        super("Snake Game");
        this.model      = m;
        this.panel      = new SnakePanel(model);
        this.controller = new SnakeController(model, panel, this);
        this.device     = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        /* ----------- 叠放面板与按钮 ----------- */
        JLayeredPane lp = new JLayeredPane();
        int w = m.getCols() * Constants.UNIT;
        int h = m.getRows() * Constants.UNIT;
        panel.setBounds(0, 0, w, h);

        gear = new JButton(new GearIcon(24, Color.BLACK));
        gear.setBorderPainted(false);
        gear.setContentAreaFilled(false);
        gear.setFocusPainted(false);                 // ★ 不抢键盘焦点
        gear.setFocusable(false);                 // ★ 不抢键盘焦点
        lp.setPreferredSize(new Dimension(w, h));
        lp.add(panel, Integer.valueOf(0));
        lp.add(gear , Integer.valueOf(1));
        add(lp);

        gear.addActionListener(e -> toggleSettings());

        /* ---------- 键盘快捷键 ---------- */
        setupKeyboardShortcuts();

        /* ---------- 注册事件监听器 ---------- */
        registerEventListeners();

        /* ---------- 常规初始化 ---------- */
        pack();
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        /* ★★ 关键：窗口尺寸就绪后再定位齿轮 ★★ */
        positionGear();
        // 若今后想支持窗口拉伸，可给 panel 再加 ComponentListener 调同一方法

        setVisible(true);
        panel.requestFocusInWindow();             // ★ 把焦点给游戏面板
        controller.start();
    }
    
    private void setupKeyboardShortcuts() {
        /* ---------- 让 ESC 打开设置 ---------- */
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "toggle");
        getRootPane().getActionMap().put("toggle",
            new AbstractAction(){public void actionPerformed(ActionEvent e){
                toggleSettings(); }});
                
        /* ---------- F11 全屏切换 ---------- */
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "fullscreen");
        getRootPane().getActionMap().put("fullscreen",
            new AbstractAction(){public void actionPerformed(ActionEvent e){
                toggleFullScreen(); }});
                
        /* ---------- F5 快速保存 ---------- */
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "quicksave");
        getRootPane().getActionMap().put("quicksave",
            new AbstractAction(){public void actionPerformed(ActionEvent e){
                quickSave(); }});
                
        /* ---------- F9 快速加载 ---------- */
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "quickload");
        getRootPane().getActionMap().put("quickload",
            new AbstractAction(){public void actionPerformed(ActionEvent e){
                quickLoad(); }});
    }
    
    private void registerEventListeners() {
        EventManager eventManager = EventManager.getInstance();
        
        // 监听加载游戏事件
        eventManager.addEventListener(GameEvent.EventType.LOAD_GAME, event -> {
            SnakeModel loadedModel = event.getData(SnakeModel.class);
            if (loadedModel != null) {
                // 重新创建游戏窗口
                dispose();
                new SnakeGameWindow(loadedModel);
            }
        });
        
        // 监听游戏结束事件
        eventManager.addEventListener(GameEvent.EventType.GAME_OVER, event -> {
            // 游戏结束时显示最终分数
            int finalScore = (Integer) event.getData();
            showGameOverDialog(finalScore);
        });
    }
    
    private void showGameOverDialog(int finalScore) {
        SwingUtilities.invokeLater(() -> {
            String message = String.format("Game Over!\nFinal Score: %d\nHigh Score: %d", 
                finalScore, model.getHighScore());
            int option = JOptionPane.showConfirmDialog(this, message, "Game Over", 
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                // 重新开始游戏
                model.reset();
                controller.resume();
            }
        });
    }
    
    private void toggleFullScreen() {
        if (isFullScreen) {
            // 退出全屏
            device.setFullScreenWindow(null);
            isFullScreen = false;
            setResizable(false);
            pack();
            setLocationRelativeTo(null);
        } else {
            // 进入全屏
            setResizable(true);
            device.setFullScreenWindow(this);
            isFullScreen = true;
        }
        positionGear();
        panel.requestFocusInWindow();
    }
    
    private void quickSave() {
        if (SaveData.autoSave(model)) {
            showStatusMessage("Game saved successfully!");
        } else {
            showStatusMessage("Failed to save game!");
        }
    }
    
    private void quickLoad() {
        SnakeModel loadedModel = SaveData.load(0);
        if (loadedModel != null) {
            // 重新创建游戏窗口
            dispose();
            new SnakeGameWindow(loadedModel);
        } else {
            showStatusMessage("No save file found!");
        }
    }
    
    private void showStatusMessage(String message) {
        // 创建一个临时状态栏显示消息
        JWindow statusWindow = new JWindow(this);
        JLabel label = new JLabel(message);
        label.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 14));
        label.setForeground(Constants.COLOR_TEXT);
        label.setBackground(Constants.COLOR_BACKGROUND);
        label.setOpaque(true);
        label.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        statusWindow.setContentPane(label);
        statusWindow.pack();
        statusWindow.setLocationRelativeTo(this);
        statusWindow.setVisible(true);
        
        // 2秒后自动消失
        Timer timer = new Timer(2000, e -> statusWindow.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    /** 把齿轮固定在右上角，留 4px 边距 */
    private void positionGear() {
        int gearSize = 24;
        gear.setBounds(panel.getWidth() - gearSize - 4,
                       4,
                       gearSize,
                       gearSize);
    }

    private void toggleSettings() {
        controller.pause();
    
        if (settings == null)
            settings = new SettingsDialog(this, model, controller);
    
        settings.setLocationRelativeTo(this);
        settings.setVisible(true);          // 模态；此行返回时对话框已关
    
        panel.requestFocusInWindow();       // ★ 关键：让 SnakePanel 重新拿到键盘焦点
    }

    /* 供 MainMenu 调用的辅助方法 */
    public void bringToFront() {
        setState(JFrame.NORMAL);
        toFront();
        requestFocus();
    }
    public boolean isRunning() {
        return isShowing() && model.getState() == GameState.RUNNING;
    }
}