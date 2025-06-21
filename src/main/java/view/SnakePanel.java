package snake.view;

import snake.model.SnakeModel;
import snake.util.Constants;
import snake.util.GameState;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import snake.model.EnemySnake;

public class SnakePanel extends JPanel {
    private final SnakeModel model;
    private BufferedImage buffer;           // 双缓冲图像
    private Graphics2D bufferGraphics;      // 缓冲图形上下文
    private Image gridCache;                // 背景网格缓存

    public SnakePanel(SnakeModel m) {
        this.model = m;
        setPreferredSize(new Dimension(m.getCols() * Constants.UNIT,
                                       m.getRows() * Constants.UNIT));

        setBackground(Constants.COLOR_THEME_BG);
        setOpaque(true);
        setFocusable(true);
        
        // 启用双缓冲
        setDoubleBuffered(true);
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        
        // 检查是否需要重新创建缓冲
        if (buffer == null || buffer.getWidth() != getWidth() || buffer.getHeight() != getHeight()) {
            createBuffer();
        }
        
        // 总是用当前的游戏状态重绘缓冲区
        drawToBuffer();
        
        // 将缓冲内容绘制到屏幕
        g0.drawImage(buffer, 0, 0, null);
    }
    
    private void createBuffer() {
        buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        bufferGraphics = buffer.createGraphics();
        bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    
    private void drawToBuffer() {
        if (bufferGraphics == null) return;
        
        // 清除缓冲
        bufferGraphics.setColor(Constants.COLOR_THEME_BG);
        bufferGraphics.fillRect(0, 0, getWidth(), getHeight());

        drawGrid(bufferGraphics);
        drawSnakeAndFood(bufferGraphics);

        if (model.getState() == GameState.GAME_OVER) {
            drawGameOver(bufferGraphics);
        }
    }
    
    /* ------- 私有绘制方法 ------- */
    private void drawGrid(Graphics2D g) {
        if (gridCache == null) {
            int w = model.getCols() * Constants.UNIT;
            int h = model.getRows() * Constants.UNIT;
            gridCache = createImage(w, h);
            Graphics2D bg = (Graphics2D) gridCache.getGraphics();
            bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            bg.setColor(Constants.COLOR_THEME_GRID);
            for (int x = 0; x <= w; x += Constants.UNIT) bg.drawLine(x, 0, x, h);
            for (int y = 0; y <= h; y += Constants.UNIT) bg.drawLine(0, y, w, y);
            bg.dispose();
        }
        g.drawImage(gridCache, 0, 0, null);
    }

    private void drawSnakeAndFood(Graphics2D g) {
        /* ---- 画墙 ---- */
        g.setColor(Constants.COLOR_WALL);
        for (Point w : model.getWalls()) {
            g.fillRect(w.x * Constants.UNIT, w.y * Constants.UNIT,
                    Constants.UNIT, Constants.UNIT);
        }
        
        /* ---- 画蛇 ---- */
        // 优化：使用渐变效果
        int bodySize = model.getBody().size();
        int index = 0;
        for (Point p : model.getBody()) {
            // 蛇头使用不同颜色
            if (index == 0) {
                g.setColor(Constants.COLOR_SNAKE_HEAD); // 蛇头
            } else {
                // 蛇身渐变效果
                float ratio = (float) index / bodySize;
                Color baseColor = Constants.COLOR_SNAKE_BODY;
                int red = baseColor.getRed();
                int green = (int) (baseColor.getGreen() * (1 - ratio * 0.5));
                int blue = baseColor.getBlue();
                g.setColor(new Color(red, green, blue));
            }
            g.fillRect(p.x * Constants.UNIT, p.y * Constants.UNIT,
                       Constants.UNIT, Constants.UNIT);
            index++;
        }

        /* ---- 画敌人 ---- */
        for (EnemySnake enemy : model.getEnemies()) {
            // 根据AI策略选择颜色
            Color bodyColor, headColor;
            switch (enemy.getStrategy()) {
                case AGGRESSIVE:
                    bodyColor = new Color(200, 50, 50);  // 红色 - 攻击性
                    headColor = new Color(255, 100, 100);
                    break;
                case DEFENSIVE:
                    bodyColor = new Color(50, 50, 200);  // 蓝色 - 防御性
                    headColor = new Color(100, 100, 255);
                    break;
                case FOOD_HUNTER:
                    bodyColor = new Color(200, 200, 50); // 黄色 - 食物猎人
                    headColor = new Color(255, 255, 100);
                    break;
                case RANDOM:
                default:
                    bodyColor = new Color(100, 100, 100); // 灰色 - 随机
                    headColor = new Color(150, 150, 150);
                    break;
            }
            
            // 绘制身体
            g.setColor(bodyColor);
            for (Point p : enemy.getBody()) {
                g.fillRect(p.x * Constants.UNIT, p.y * Constants.UNIT, Constants.UNIT, Constants.UNIT);
            }
            // 绘制头部
            g.setColor(headColor);
            Point head = enemy.getBody().peekFirst();
            if (head != null) {
                g.fillRect(head.x * Constants.UNIT, head.y * Constants.UNIT, Constants.UNIT, Constants.UNIT);
            }
        }

        /* ---- 画果子 ---- */
        SnakeModel.Fruit f = model.getFruit();
        if (f != null) {
            g.setColor(f.type.color);
            for (Point p : f.cells) {
                g.fillOval(p.x * Constants.UNIT + 2, p.y * Constants.UNIT + 2,
                           Constants.UNIT - 4, Constants.UNIT - 4);
            }
        }

        // HUD - 优化显示
        g.setColor(Constants.COLOR_THEME_TEXT);
        g.setFont(Constants.FONT_UI_DEFAULT);
        g.drawString("Score: " + model.getScore(), 10, 20);
        
        // 添加蛇长度显示
        g.drawString("Length: " + model.getBody().size(), 10, 40);
    }

    private void drawGameOver(Graphics2D g) {
        // 半透明背景
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        String msg = "Game Over - Press SPACE";
        g.setColor(Constants.COLOR_THEME_TEXT);
        g.setFont(Constants.FONT_UI_TITLE);
        int w = g.getFontMetrics().stringWidth(msg);
        int midX = (getWidth() - w) / 2;
        int midY = getHeight() / 2;
        g.drawString(msg, midX, midY);
        
        // 显示最终分数
        String scoreMsg = "Final Score: " + model.getScore();
        g.setFont(Constants.FONT_UI_DEFAULT);
        int scoreW = g.getFontMetrics().stringWidth(scoreMsg);
        int scoreX = (getWidth() - scoreW) / 2;
        g.drawString(scoreMsg, scoreX, midY + 40);
    }
}