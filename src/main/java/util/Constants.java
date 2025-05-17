package src.main.java.util;

import java.awt.Color;

public final class Constants {
    private Constants() {}
    public static final int UNIT  = 20;   // 一个格子的像素
    public static final int DELAY = 100;  // 毫秒，游戏节拍

    public static final Color COLOR_FRUIT_1 = new Color(255,  30, 30);  // 小红果
    public static final Color COLOR_FRUIT_4 = new Color(255, 140,  0);  // 橙色 2×2 大果
    public static final int[] WEIGHTS = { 70, 30 };  // ONE, FOUR 出现概率 %

    public static final Color COLOR_WALL = new Color(80, 80, 80);  // 深灰
    public static final double WALL_PERCENT = 0.02;                // 5 %
}
