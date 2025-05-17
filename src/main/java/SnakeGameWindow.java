package src.main.java;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import src.main.java.controller.SnakeController;
import src.main.java.model.SnakeModel;
import src.main.java.util.Constants;
import src.main.java.util.GameState;
import src.main.java.view.SnakePanel;
import src.main.java.view.SettingsDialog;
import src.main.java.view.GearIcon;

public class SnakeGameWindow extends JFrame {
    private final SnakeModel     model;
    private final SnakePanel     panel;
    private final SnakeController controller;
    private       SettingsDialog settings;
    private JButton gear;

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

        /* ----------- 叠放面板与按钮 ----------- */
        JLayeredPane lp = new JLayeredPane();
        int w = m.getCols() * Constants.UNIT;
        int h = m.getRows() * Constants.UNIT;
        panel.setBounds(0, 0, w, h);

        gear = new JButton(new GearIcon(24, Color.BLACK));
        gear.setBorderPainted(false);
        gear.setContentAreaFilled(false);
        gear.setFocusPainted(false);
        gear.setFocusable(false);                 // ★ 不抢键盘焦点
        lp.setPreferredSize(new Dimension(w, h));
        lp.add(panel, Integer.valueOf(0));
        lp.add(gear , Integer.valueOf(1));
        add(lp);

        gear.addActionListener(e -> toggleSettings());

        /* ---------- 让 ESC 打开设置 ---------- */
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "toggle");
        getRootPane().getActionMap().put("toggle",
            new AbstractAction(){public void actionPerformed(ActionEvent e){
                toggleSettings(); }});

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