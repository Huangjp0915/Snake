package src.main.java;
import javax.swing.*;

import src.main.java.controller.SnakeController;
import src.main.java.model.SnakeModel;
import src.main.java.util.Constants;
import src.main.java.util.GameState;
import src.main.java.view.SnakePanel;
import src.main.java.SaveData;

public class SnakeGameWindow extends JFrame {
    private final SnakeModel     model;
    private final SnakePanel     panel;
    private final SnakeController controller;

    /* 新游戏 */
    public SnakeGameWindow(int width, int height) {
        this(new SnakeModel(width / Constants.UNIT,
                            height / Constants.UNIT));
    }

    /* 继续游戏 / 载入存档 */
    public SnakeGameWindow(SnakeModel m) {
        super("Snake Game");
        this.model = m;
        this.panel = new SnakePanel(model);
        this.controller = new SnakeController(model, panel, this);

        add(panel);
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                controller.shutdown();   // 关闭计时器
                SaveData.write(model);   // 写入存档
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);

        controller.start();
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