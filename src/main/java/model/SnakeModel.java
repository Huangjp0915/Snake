/* snake/model/SnakeModel.java */
package snake.model;

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
import java.util.Iterator;

import snake.util.Direction;
import snake.util.GameState;
import snake.util.Constants;
import snake.util.EventManager;
import snake.util.GameEvent;

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
    private int highScore = 0;  // 历史最高分

    private final Set<Point> walls = new HashSet<>();
    private int nextWallScore = Constants.WALL_SPAWN_SCORE; 

    /* ======== 敌人系统 ======== */
    private final List<EnemySnake> enemies = new ArrayList<>();

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
        nextWallScore = Constants.WALL_SPAWN_SCORE;     // 重新从配置的分数开始生成墙
        enemies.clear();        // 清空敌人
        spawnFruit();
        
        // 触发游戏开始事件
        EventManager.getInstance().fireEvent(this, GameEvent.EventType.GAME_START);
    }

    public void tick() {
        if (state != GameState.RUNNING) return;

        // 1. 移动所有单位
        movePlayerSnake();
        moveEnemies();

        // 2. 检查碰撞并处理后果
        checkAllCollisions();

        // 3. 根据游戏状态生成新物体
        spawnNewObjects();
    }

    private void movePlayerSnake() {
        if (body.isEmpty()) return;

        // 计算新头部位置
        Point newHead = new Point(body.peekFirst());
        newHead.translate(dir.dx, dir.dy);
        newHead = wrap(newHead);

        // 如果需要，移除尾巴
        if (growth > 0) {
            growth--;
        } else {
            body.removeLast();
        }

        // 添加新头部
        body.addFirst(newHead);
        EventManager.getInstance().fireEvent(this, GameEvent.EventType.SNAKE_MOVED, newHead);

        // 检查吃果子
        if (fruit != null && fruit.cells.contains(newHead)) {
            int oldScore = score;
            score  += fruit.type.score;
            growth += fruit.type.score;
            
            EventManager.getInstance().fireEvent(this, GameEvent.EventType.FOOD_EATEN, fruit.type);
            if (score != oldScore) {
                EventManager.getInstance().fireEvent(this, GameEvent.EventType.SCORE_CHANGED, score);
            }
            spawnFruit();
        }
    }

    private void moveEnemies() {
        // 先移动所有敌人
        for (EnemySnake enemy : enemies) {
            enemy.update(this);
        }

        // 然后检查吃果子
        for (EnemySnake enemy : enemies) {
            if (!enemy.isAlive()) continue;

            Point enemyHead = enemy.getBody().peekFirst();
            if (fruit != null && enemyHead != null && fruit.cells.contains(enemyHead)) {
                enemy.grow(fruit.type.size);
                
                // 敌人吃了果子后，果子消失并生成新的
                spawnFruit();

                // 因为果子已经没了，所以跳出循环
                break; 
            }
        }
    }

    private void checkAllCollisions() {
        Point playerHead = body.peekFirst();
        if (playerHead == null) { // 如果玩家蛇不存在（理论上不应发生）
            setGameOver();
            return;
        }

        // 1. 玩家撞墙
        if (walls.contains(playerHead)) {
            setGameOver();
            return;
        }

        // 2. 玩家撞自己
        Iterator<Point> bodyIter = body.iterator();
        bodyIter.next(); // 跳过头部
        while (bodyIter.hasNext()) {
            if (playerHead.equals(bodyIter.next())) {
                setGameOver();
                return;
            }
        }

        // 3. 玩家与敌人碰撞 (头撞身体 或 头撞头)
        for (EnemySnake enemy : enemies) {
            if (enemy.getBody().contains(playerHead)) {
                setGameOver();
                return;
            }
        }
        
        // 4. 敌人之间的碰撞
        List<EnemySnake> enemiesToRemove = new ArrayList<>();
        for (int i = 0; i < enemies.size(); i++) {
            EnemySnake currentEnemy = enemies.get(i);
            if (!currentEnemy.isAlive()) continue;
            
            Point currentHead = currentEnemy.getBody().peekFirst();

            // 4a. 敌人撞墙
            if (walls.contains(currentHead)) {
                currentEnemy.setAlive(false);
                continue;
            }
            
            // 4b. 敌人撞玩家身体
            if (body.contains(currentHead)) {
                 currentEnemy.setAlive(false);
                 continue;
            }

            // 4c. 敌人互撞 (头撞身体 或 头撞头)
            for (int j = 0; j < enemies.size(); j++) {
                if (i == j) continue;
                EnemySnake otherEnemy = enemies.get(j);
                if (otherEnemy.getBody().contains(currentHead)) {
                    currentEnemy.setAlive(false);
                    break; 
                }
            }
        }

        enemies.removeIf(enemy -> !enemy.isAlive());
    }

    private void spawnNewObjects() {
        // 生成墙
        if (score >= nextWallScore) {
            spawnWalls();
            nextWallScore += Constants.WALL_SPAWN_SCORE;
            EventManager.getInstance().fireEvent(this, GameEvent.EventType.WALL_SPAWNED, walls.size());
        }
        // 生成敌人
        if (score > 0 && (score / 10) > enemies.size()) {
            spawnEnemy();
        }
    }

    private void setGameOver() {
        state = GameState.GAME_OVER;
        if (score > highScore) {
            highScore = score;
        }
        EventManager.getInstance().fireEvent(this, GameEvent.EventType.GAME_OVER, score);
    }
    
    private void spawnEnemy() {
        Point spawnPoint;
        int attempts = 0;
        while (attempts < 100) {
            int x = rnd.nextInt(cols);
            int y = rnd.nextInt(rows);
            spawnPoint = new Point(x, y);

            if (isAreaSafe(spawnPoint, 5)) {
                // 随机选择AI策略
                EnemySnake.AIStrategy[] strategies = EnemySnake.AIStrategy.values();
                EnemySnake.AIStrategy strategy = strategies[rnd.nextInt(strategies.length)];
                
                // 根据分数调整策略概率，让游戏更有挑战性
                if (score > 50) {
                    // 高分时增加攻击性策略的概率
                    if (rnd.nextDouble() < 0.7) {
                        strategy = EnemySnake.AIStrategy.AGGRESSIVE;
                    }
                }
                
                enemies.add(new EnemySnake(spawnPoint, 5, strategy));
                EventManager.getInstance().fireEvent(this, GameEvent.EventType.ENEMY_SPAWNED, strategy);
                return;
            }
            attempts++;
        }
        System.err.println("Could not find a safe place to spawn an enemy.");
    }
    
    private boolean isAreaSafe(Point center, int radius) {
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                Point p = new Point(center.x + i, center.y + j);
                if (walls.contains(p) || body.contains(p) || fruit.cells.contains(p)) {
                    return false;
                }
                for (EnemySnake enemy : enemies) {
                    if (enemy.getBody().contains(p)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void spawnWalls() {
        int totalCells = cols * rows;
        int maxWalls = (int) (totalCells * Constants.MAX_WALL_PERCENT);
        
        // 限制最大墙壁数量
        if (walls.size() >= maxWalls) {
            return;
        }
        
        int toAdd = (int) Math.round(totalCells * Constants.WALL_PERCENT);
    
        /* -------- 不再清空旧墙，直接累计 -------- */
        int targetSize = Math.min(walls.size() + toAdd, maxWalls);  // 目标墙数 = 旧墙 + 本轮新增，但不超过最大值
    
        while (walls.size() < targetSize) {
            Point p = new Point(rnd.nextInt(cols), rnd.nextInt(rows));
    
            if (body.contains(p)) continue;             // 不盖蛇身
            if (fruit != null && fruit.cells.contains(p)) continue; // 不盖当前果子
            walls.add(p);                               // Set 保证不重复
        }
    }

    /* ===== 行为 ===== */
    public void turn(Direction d){ 
        if (!d.isOpposite(dir)) {
            dir = d;
        }
    }
    
    private Point wrap(Point p){ 
        return new Point( (p.x+cols)%cols, (p.y+rows)%rows ); 
    }

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
        int attempts = 0;
        final int MAX_ATTEMPTS = 100; // 防止无限循环
        
        while(attempts < MAX_ATTEMPTS){
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
            
            // 检查是否与蛇身重叠
            boolean overlapWithSnake = cells.stream().anyMatch(body::contains);
            if (overlapWithSnake) {
                attempts++;
                continue;
            }

            /* ② ★★ 新增这一行：避开墙 ★★ */
            boolean overlapWithWall = cells.stream().anyMatch(walls::contains);
            if (overlapWithWall) {
                attempts++;
                continue;
            }

            break;   // 合法，跳出循环
        }
        
        // 如果尝试次数过多，说明空间不足，不生成果子
        if (attempts >= MAX_ATTEMPTS) {
            fruit = null;
        } else {
            fruit = new Fruit(type, cells);
        }
    }

    /* ===== Getters ===== */
    public ArrayDeque<Point> getBody(){ return body; }
    public List<EnemySnake> getEnemies() { return enemies; }
    public Fruit            getFruit(){ return fruit; }
    public Set<Point>       getWalls() { return walls; }
    public int              getScore(){ return score; }
    public int              getHighScore(){ return highScore; }
    public GameState        getState(){ return state; }
    public int              getCols(){ return cols; }
    public int              getRows(){ return rows; }

    public void setState(GameState s){ 
        this.state = s; 
        if (s == GameState.PAUSED) {
            EventManager.getInstance().fireEvent(this, GameEvent.EventType.GAME_PAUSE);
        } else if (s == GameState.RUNNING) {
            EventManager.getInstance().fireEvent(this, GameEvent.EventType.GAME_RESUME);
        }
    }
    
    public void setHighScore(int score) {
        this.highScore = score;
    }
}
