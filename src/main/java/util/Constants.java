package snake.util;

import java.awt.Color;
import java.awt.Font;

public final class Constants {
    private Constants() {}
    
    /* ===== 基础游戏配置 ===== */
    public static final int UNIT  = 20;   // 一个格子的像素
    public static final int DELAY = 100;  // 毫秒，游戏节拍
    
    /* ===== 游戏速度配置 ===== */
    public static final int SPEED_SLOW = 150;    // 慢速
    public static final int SPEED_NORMAL = 100;  // 正常速度
    public static final int SPEED_FAST = 70;     // 快速
    public static final int SPEED_EXTREME = 50;  // 极速
    
    /* ===== 颜色主题配置 ===== */
    public static final Color COLOR_FRUIT_1 = new Color(255,  30, 30);  // 小红果
    public static final Color COLOR_FRUIT_4 = new Color(255, 140,  0);  // 橙色 2×2 大果
    public static final int[] WEIGHTS = { 70, 30 };  // ONE, FOUR 出现概率 %
    
    public static final Color COLOR_WALL = new Color(80, 80, 80);  // 深灰
    public static final double WALL_PERCENT = 0.02;                // 2%
    
    /* ===== 蛇的颜色配置 ===== */
    public static final Color COLOR_SNAKE_HEAD = Color.YELLOW;
    public static final Color COLOR_SNAKE_BODY = Color.GREEN;
    
    /* ===== 界面配置 ===== */
    public static final Color COLOR_BACKGROUND = Color.BLACK;
    public static final Color COLOR_GRID = Color.DARK_GRAY;
    public static final Color COLOR_TEXT = Color.WHITE;
    
    /* ===== 游戏窗口尺寸配置 ===== */
    public static final int WINDOW_SMALL = 400;
    public static final int WINDOW_MEDIUM = 600;
    public static final int WINDOW_LARGE = 800;
    
    /* ===== 存档配置 ===== */
    public static final int MAX_SAVE_SLOTS = 5;
    public static final String SAVE_FILE_PREFIX = "snake_save_";
    public static final String SAVE_FILE_SUFFIX = ".dat";
    
    /* ===== 音效配置 ===== */
    public static final boolean SOUND_ENABLED = true;
    public static final float SOUND_VOLUME = 0.7f;

    /* ===== Modern Dark Theme ===== */
    public static final Color COLOR_THEME_BG = new Color(24, 28, 38);
    public static final Color COLOR_THEME_PANEL = new Color(40, 46, 62);
    public static final Color COLOR_THEME_BUTTON = new Color(88, 101, 242);
    public static final Color COLOR_THEME_BUTTON_HOVER = new Color(71, 82, 196);
    public static final Color COLOR_THEME_TEXT = Color.WHITE;
    public static final Color COLOR_THEME_GRID = new Color(34, 38, 48);
    public static final Font FONT_UI_DEFAULT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_UI_TITLE = new Font("Segoe UI", Font.BOLD, 32);
    
    /* ===== Enemy Snake Colors ===== */
    public static final Color COLOR_ENEMY_HEAD = new Color(227, 52, 47); // Red
    public static final Color COLOR_ENEMY_BODY = new Color(255, 128, 0); // Orange

    /* ===== 游戏难度配置 ===== */
    public static final int WALL_SPAWN_SCORE = 10;  // 每10分生成一次墙
    public static final double MAX_WALL_PERCENT = 0.15; // 最大墙壁比例15%
}
