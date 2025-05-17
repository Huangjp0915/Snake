package src.main.java.view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class GearIcon implements Icon {
    private final int   size;          // 正方形边长
    private final Color color;         // 齿轮颜色

    public GearIcon(int size, Color color) {
        this.size  = size;
        this.color = color;
    }

    @Override public int  getIconWidth()  { return size; }
    @Override public int  getIconHeight() { return size; }

    @Override
    public void paintIcon(Component c, Graphics g0, int x, int y) {
        Graphics2D g = (Graphics2D) g0.create();
        g.translate(x, y);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(color);

        int rOut = size / 2;           // 外半径
        int rIn  = (int) (rOut * 0.6); // 齿根半径
        int rHub = (int) (rOut * 0.35);// 中心圆半径
        int teeth = 8;                 // 齿数
        double step = Math.PI * 2 / teeth;

        Path2D gear = new Path2D.Double();
        for (int i = 0; i < teeth; i++) {
            double angle = i * step;
            double ax = rOut * Math.cos(angle);
            double ay = rOut * Math.sin(angle);
            double bx = rIn  * Math.cos(angle + step / 2);
            double by = rIn  * Math.sin(angle + step / 2);
            if (i == 0) gear.moveTo(rOut + ax, rOut + ay);
            else        gear.lineTo(rOut + ax, rOut + ay);
            gear.lineTo(rOut + bx, rOut + by);
        }
        gear.closePath();
        g.fill(gear);                  // 齿圈
        g.fillOval(rOut - rHub, rOut - rHub, 2 * rHub, 2 * rHub); // 中心

        g.dispose();
    }
}
