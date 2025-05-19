package src.main.java.view;

import src.main.java.model.SnakeModel;
import src.main.java.util.Constants;
import src.main.java.util.GameState;
import javax.swing.*;
import java.awt.*;

public class SnakePanel extends JPanel {
    private final SnakeModel model;
    private Image gridCache;               // 背景网格缓存

    public SnakePanel(SnakeModel m) {
        this.model = m;
        setPreferredSize(new Dimension(m.getCols() * Constants.UNIT,
                                       m.getRows() * Constants.UNIT));

        setBackground(Color.BLACK);   // 面板底色
        setOpaque(true);
        setFocusable(true);
    }

    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        g.setColor(Color.BLACK);            // 明确指定颜色
        g.fillRect(0, 0, getWidth(), getHeight());  // 整个面板涂黑

        drawGrid(g);
        drawSnakeAndFood(g);

        if (model.getState() == GameState.GAME_OVER) drawGameOver(g);
    }

    /* ------- 私有绘制方法 ------- */
    private void drawGrid(Graphics2D g) {
        if (gridCache == null) {
            int w = model.getCols() * Constants.UNIT;
            int h = model.getRows() * Constants.UNIT;
            gridCache = createImage(w, h);
            Graphics2D bg = (Graphics2D) gridCache.getGraphics();
            bg.setColor(Color.DARK_GRAY);
            for (int x = 0; x <= w; x += Constants.UNIT) bg.drawLine(x, 0, x, h);
            for (int y = 0; y <= h; y += Constants.UNIT) bg.drawLine(0, y, w, y);
            bg.dispose();
        }
        g.drawImage(gridCache, 0, 0, null);
    }

    private void drawSnakeAndFood(Graphics2D g) {
        /* ---- 画墙 ---- */
        g.setColor(Constants.COLOR_WALL);
        for (Point w : model.getWalls())
            g.fillRect(w.x * Constants.UNIT, w.y * Constants.UNIT,
                    Constants.UNIT, Constants.UNIT);
        /* ---- 画蛇 ---- */
        g.setColor(Color.GREEN);
        for (Point p : model.getBody())
            g.fillRect(p.x * Constants.UNIT,
                       p.y * Constants.UNIT,
                       Constants.UNIT, Constants.UNIT);

        /* ---- 画果子 ---- */
        SnakeModel.Fruit f = model.getFruit();
        g.setColor(f.type.color);
        for (Point p : f.cells)
            g.fillOval(p.x * Constants.UNIT + 2,
                       p.y * Constants.UNIT + 2,
                       Constants.UNIT - 4, Constants.UNIT - 4);

        // HUD
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 16));
        g.drawString("Score: " + model.getScore(), 10, 20);
    }

    private void drawGameOver(Graphics2D g) {
        String msg = "Game Over - Press SPACE";
        g.setColor(Color.WHITE);
        g.setFont(new Font("Consolas", Font.BOLD, 32));
        int w = g.getFontMetrics().stringWidth(msg);
        int midX = (getWidth() - w) / 2;
        int midY = getHeight() / 2;
        g.drawString(msg, midX, midY);
    }
}