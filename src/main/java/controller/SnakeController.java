package src.main.java.controller;

import src.main.java.model.SnakeModel;
import src.main.java.util.Constants;
import src.main.java.util.Direction;
import src.main.java.util.GameState;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.*;

public class SnakeController {
    private final SnakeModel model;
    private final JPanel     panel;
    private Timer            timer;      // Swing Timer 代替 ScheduledExecutorService

    public SnakeController(SnakeModel m, JPanel p, JFrame window) {
        this.model = m;
        this.panel = p;

        KeyAdapter keys = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP,    KeyEvent.VK_W -> model.turn(Direction.UP);
                    case KeyEvent.VK_DOWN,  KeyEvent.VK_S -> model.turn(Direction.DOWN);
                    case KeyEvent.VK_LEFT,  KeyEvent.VK_A -> model.turn(Direction.LEFT);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> model.turn(Direction.RIGHT);
                    case KeyEvent.VK_SPACE -> {
                        if (model.getState() == GameState.GAME_OVER) model.reset();
                    }
                }
            }
        };

        panel.addKeyListener(keys);
    }

    public void start() {
        timer = new Timer(Constants.DELAY, e -> {
            model.tick();        // 逻辑
            panel.repaint();     // 绘制
        });
        timer.start();           // **两者都在 EDT 中运行**
    }

    public void pause()  { if (timer != null) timer.stop();  }
    public void resume() { if (timer != null) timer.start(); }
    public void shutdown() { if (timer != null) timer.stop(); }
}