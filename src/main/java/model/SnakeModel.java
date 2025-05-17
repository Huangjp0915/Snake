/* src/main/java/model/SnakeModel.java */
package src.main.java.model;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import src.main.java.util.Direction;
import src.main.java.util.GameState;
import src.main.java.util.Constants;

public class SnakeModel implements Serializable {
    private static final long serialVersionUID = 1L;

    /* ===== 基础字段 ===== */
    private final int cols, rows;
    private final Random rnd = new Random();

    private final ArrayDeque<Point> body = new ArrayDeque<>();
    private Direction dir  = Direction.RIGHT;
    private GameState state = GameState.RUNNING;

    private int score  = 0;
    private int growth = 0;

    private final Set<Point> walls = new HashSet<>();
    private int nextWallScore = 10; 

    /* ======== 果子系统 ======== */
    public enum FruitType {
        ONE (1, 1,  Constants.COLOR_FRUIT_1),
        FOUR(4, 4,  Constants.COLOR_FRUIT_4);
        // 以后 S / A / B / C / D 再往下加即可

        public final int size, score;
        public final Color color;
        FruitType(int s, int p, Color c){ size=s; score=p; color=c; }
    }
    public static class Fruit implements Serializable { 
        private static final long serialVersionUID = 1L; 
    
        public final FruitType type;
        public final List<Point> cells;
    
        Fruit(FruitType t, List<Point> c) {
            this.type  = t;
            this.cells = c;
        }
    }
    private Fruit fruit;                       // 当前果子

    /* ===== 构造 & 重置 ===== */
    public SnakeModel(int cols, int rows) {
        this.cols = cols; this.rows = rows;
        reset();
    }
    public void reset() {
        body.clear();
        body.add(new Point(cols/2, rows/2));
        dir   = Direction.RIGHT;
        score = 0;
        growth= 0;
        state = GameState.RUNNING;
        walls.clear();          // ← 每次新开局都没有旧墙残留
        nextWallScore = 10;     // 重新从 10 分开始生成墙
        spawnFruit();
    }

    /* ===== 每一帧 ===== */
    public void tick() {
        if (state != GameState.RUNNING) return;
    
        /* ① 先算出新头坐标（此时 body 仍有元素） */
        Point newHead = new Point(body.peekFirst());      // peek 不会抛异常
        newHead.translate(dir.dx, dir.dy);
        newHead = wrap(newHead);
    
        /* ② 预先记录当前尾巴，用来判断“头追尾”是否合法 */
        Point tail = body.peekLast();
    
        /* ③ 处理尾巴或生长 */
        if (growth == 0) {
            body.removeLast();          // 删尾巴（可能把队列清空）
        } else {
            growth--;
        }

        boolean hitWall  = walls.contains(newHead);
        boolean hitBody  = body.contains(newHead);

        if (hitWall || hitBody) {
            state = GameState.GAME_OVER;
            return;
        }
    
        /* ④ 撞身判定：忽略本 tick 已经移走的尾巴格 */
        if (hitBody) {                      // 真撞到身体
            state = GameState.GAME_OVER;
            return;
        }
    
        /* ⑤ 插入新头 */
        body.addFirst(newHead);
    
        /* ⑥ 吃果子 */
        if (fruit.cells.contains(newHead)) {
            score  += fruit.type.score;
            growth += fruit.type.score;     // 接下来 n tick 不删尾巴
            spawnFruit();
        }

        if (score >= nextWallScore) {
            spawnWalls();
            nextWallScore += 10;                 // 下一次门槛
        }
    }

    private void spawnWalls() {
        int totalCells = cols * rows;
        int toAdd      = (int) Math.round(totalCells * Constants.WALL_PERCENT);
    
        /* -------- 不再清空旧墙，直接累计 -------- */
        int targetSize = walls.size() + toAdd;          // 目标墙数 = 旧墙 + 本轮新增
    
        while (walls.size() < targetSize) {
            Point p = new Point(rnd.nextInt(cols), rnd.nextInt(rows));
    
            if (body.contains(p)) continue;             // 不盖蛇身
            if (fruit != null && fruit.cells.contains(p)) continue; // 不盖当前果子
            walls.add(p);                               // Set 保证不重复
        }
    }

    /* ===== 行为 ===== */
    public void turn(Direction d){ if (!d.isOpposite(dir)) dir = d; }
    private Point wrap(Point p){ return new Point( (p.x+cols)%cols, (p.y+rows)%rows ); }

    /* ===== 生成果子 ===== */
    private void spawnFruit(){
        /* 1. 按权重随机类型 */
        int total = Arrays.stream(Constants.WEIGHTS).sum();
        int pick  = rnd.nextInt(total);
        int acc   = 0;  FruitType type = FruitType.ONE;
        for(int i=0;i<Constants.WEIGHTS.length;i++){
            acc += Constants.WEIGHTS[i];
            if(pick < acc){ type = FruitType.values()[i]; break; }
        }

        /* 2. 选位置，确保与蛇不重叠 */
        List<Point> cells = new ArrayList<>(type.size);
        while(true){
            cells.clear();
            int x = rnd.nextInt(cols - (type==FruitType.FOUR?1:0));
            int y = rnd.nextInt(rows - (type==FruitType.FOUR?1:0));

            if(type==FruitType.ONE){
                cells.add(new Point(x,y));
            }else{                       // 2×2
                cells.addAll(Arrays.asList(
                    new Point(x,y), new Point(x+1,y),
                    new Point(x,y+1), new Point(x+1,y+1)
                ));
            }
            if (cells.stream().anyMatch(body::contains)) continue;

            /* ② ★★ 新增这一行：避开墙 ★★ */
            if (cells.stream().anyMatch(walls::contains)) continue;

            break;   // 合法，跳出循环
        }
        fruit = new Fruit(type,cells);
    }

    /* ===== Getters ===== */
    public ArrayDeque<Point> getBody(){ return body; }
    public Fruit            getFruit(){ return fruit; }
    public Set<Point>       getWalls() { return walls; }
    public int              getScore(){ return score; }
    public GameState        getState(){ return state; }
    public int              getCols(){ return cols; }
    public int              getRows(){ return rows; }

    public void setState(GameState s){ this.state = s; }
}
