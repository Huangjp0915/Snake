package snake.controller;

import snake.model.SnakeModel;
import snake.util.Constants;
import snake.util.Direction;
import snake.util.GameState;
import snake.util.GameConfig;
import snake.util.EventManager;
import snake.util.GameEvent;
import snake.SaveData;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.concurrent.*;

public class SnakeController {
    private final SnakeModel model;
    private final JPanel     panel;
    private Timer            timer;      // Swing Timer 代替 ScheduledExecutorService
    private int              autoSaveCounter = 0;  // 自动存档计数器
    private boolean          isPaused = false;

    public SnakeController(SnakeModel m, JPanel p, JFrame window) {
        this.model = m;
        this.panel = p;

        // 注册事件监听器
        registerEventListeners();

        KeyAdapter keys = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        };

        panel.addKeyListener(keys);
    }
    
    private void registerEventListeners() {
        EventManager eventManager = EventManager.getInstance();
        
        // 监听分数变化事件，用于自动存档
        eventManager.addEventListener(GameEvent.EventType.SCORE_CHANGED, event -> {
            autoSaveCounter++;
            if (GameConfig.getInstance().isAutoSaveEnabled() && 
                autoSaveCounter >= GameConfig.getInstance().getAutoSaveInterval()) {
                SaveData.autoSave(model);
                autoSaveCounter = 0;
            }
        });
        
        // 监听游戏结束事件
        eventManager.addEventListener(GameEvent.EventType.GAME_OVER, event -> {
            // 游戏结束时自动保存
            if (GameConfig.getInstance().isAutoSaveEnabled()) {
                SaveData.autoSave(model);
            }
        });
    }
    
    private void handleKeyPress(KeyEvent e) {
        GameConfig config = GameConfig.getInstance();
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                if (isControlKey(e, "up")) {
                    model.turn(Direction.UP);
                }
            }
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> {
                if (isControlKey(e, "down")) {
                    model.turn(Direction.DOWN);
                }
            }
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                if (isControlKey(e, "left")) {
                    model.turn(Direction.LEFT);
                }
            }
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                if (isControlKey(e, "right")) {
                    model.turn(Direction.RIGHT);
                }
            }
            case KeyEvent.VK_SPACE -> {
                if (isControlKey(e, "restart")) {
                    if (model.getState() == GameState.GAME_OVER) {
                        model.reset();
                        resume();
                    }
                }
            }
            case KeyEvent.VK_ESCAPE -> {
                if (isControlKey(e, "pause")) {
                    togglePause();
                }
            }
            case KeyEvent.VK_P -> {
                // 快速暂停/恢复
                togglePause();
            }
            case KeyEvent.VK_R -> {
                // 快速重启
                if (model.getState() == GameState.GAME_OVER) {
                    model.reset();
                    resume();
                }
            }
            case KeyEvent.VK_F5 -> {
                // 快速保存
                SaveData.autoSave(model);
            }
            case KeyEvent.VK_F9 -> {
                // 快速加载
                SnakeModel loadedModel = SaveData.load(0);
                if (loadedModel != null) {
                    // 这里需要通知主窗口重新加载模型
                    EventManager.getInstance().fireEvent(this, GameEvent.EventType.LOAD_GAME, loadedModel);
                }
            }
        }
    }
    
    private boolean isControlKey(KeyEvent e, String action) {
        GameConfig config = GameConfig.getInstance();
        String configuredKey = config.getControlKey(action);
        
        // 默认按键映射
        switch (action) {
            case "up": return e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W;
            case "down": return e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S;
            case "left": return e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A;
            case "right": return e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D;
            case "pause": return e.getKeyCode() == KeyEvent.VK_ESCAPE;
            case "restart": return e.getKeyCode() == KeyEvent.VK_SPACE;
            default: return false;
        }
    }
    
    private void togglePause() {
        if (model.getState() == GameState.RUNNING) {
            pause();
        } else if (model.getState() == GameState.PAUSED) {
            resume();
        }
    }

    public void start() {
        // 使用配置的游戏速度
        int speed = GameConfig.getInstance().getGameSpeed();
        timer = new Timer(speed, e -> {
            model.tick();        // 逻辑
            panel.repaint();     // 绘制
        });
        timer.start();           // **两者都在 EDT 中运行**
        
        // 触发游戏开始事件
        EventManager.getInstance().fireEvent(this, GameEvent.EventType.GAME_START);
    }

    public void pause()  { 
        if (timer != null) {
            timer.stop();  
            model.setState(GameState.PAUSED);
            isPaused = true;
        }
    }
    
    public void resume() { 
        if (timer != null) {
            timer.start(); 
            model.setState(GameState.RUNNING);
            isPaused = false;
        }
    }
    
    public void shutdown() { 
        if (timer != null) {
            timer.stop(); 
        }
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void setSpeed(int speed) {
        if (timer != null) {
            timer.setDelay(speed);
        }
    }
}