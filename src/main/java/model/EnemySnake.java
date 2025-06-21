package snake.model;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import snake.util.Direction;
import snake.util.PathFinder;

public class EnemySnake {
    private final ArrayDeque<Point> body = new ArrayDeque<>();
    private Direction direction;
    private boolean isAlive = true;
    private final Random random = new Random();
    private int growth = 0; // <-- 新增生长计数器
    
    // AI策略相关
    private final AIStrategy strategy;
    private List<Point> currentPath = new ArrayList<>();
    private int pathUpdateCounter = 0;
    private static final int PATH_UPDATE_INTERVAL = 5; // 每5帧更新一次路径
    
    // AI策略枚举
    public enum AIStrategy {
        AGGRESSIVE,    // 主动追击玩家
        DEFENSIVE,     // 躲避玩家，寻找安全区域
        FOOD_HUNTER,   // 寻找食物
        RANDOM         // 随机移动
    }
    
    public EnemySnake(Point startPos, int length) {
        this(startPos, length, AIStrategy.AGGRESSIVE);
    }
    
    public EnemySnake(Point startPos, int length, AIStrategy strategy) {
        // 创建初始身体
        body.add(startPos);
        for (int i = 1; i < length; i++) {
            body.add(new Point(startPos.x - i, startPos.y));
        }
        this.direction = Direction.RIGHT; // 初始方向
        this.strategy = strategy;
    }

    /**
     * AI核心逻辑：决定下一步行动并更新状态
     */
    public void update(SnakeModel gameState) {
        if (!isAlive) return;

        // 定期更新路径
        pathUpdateCounter++;
        if (pathUpdateCounter >= PATH_UPDATE_INTERVAL) {
            updatePath(gameState);
            pathUpdateCounter = 0;
        }

        // 决定下一个方向
        this.direction = chooseNextDirection(gameState);

        // 根据方向计算新头部位置
        Point newHead = new Point(body.peekFirst());
        newHead.translate(direction.dx, direction.dy);
        newHead = wrap(newHead, gameState.getCols(), gameState.getRows());

        // 移动蛇
        body.addFirst(newHead);

        // 根据生长计数器决定是否移除尾巴
        if (growth > 0) {
            growth--;
        } else {
            body.removeLast();
        }
    }
    
    /**
     * 更新AI路径
     */
    private void updatePath(SnakeModel gameState) {
        Point currentPos = body.peekFirst();
        if (currentPos == null) return;
        
        Set<Point> obstacles = getAllObstacles(gameState);
        
        switch (strategy) {
            case AGGRESSIVE:
                // 追击玩家头部
                Point playerHead = gameState.getBody().peekFirst();
                if (playerHead != null) {
                    currentPath = PathFinder.findPath(currentPos, playerHead, obstacles, 
                                                    gameState.getCols(), gameState.getRows());
                }
                break;
                
            case DEFENSIVE:
                // 寻找远离玩家的安全区域
                Point safeTarget = findSafeTarget(gameState, obstacles);
                if (safeTarget != null) {
                    currentPath = PathFinder.findPath(currentPos, safeTarget, obstacles, 
                                                    gameState.getCols(), gameState.getRows());
                }
                break;
                
            case FOOD_HUNTER:
                // 寻找食物
                if (gameState.getFruit() != null && !gameState.getFruit().cells.isEmpty()) {
                    Point foodPos = gameState.getFruit().cells.get(0);
                    currentPath = PathFinder.findPath(currentPos, foodPos, obstacles, 
                                                    gameState.getCols(), gameState.getRows());
                }
                break;
                
            case RANDOM:
                // 随机移动，不计算路径
                currentPath.clear();
                break;
        }
    }
    
    /**
     * 获取所有障碍物
     */
    private Set<Point> getAllObstacles(SnakeModel gameState) {
        Set<Point> obstacles = new java.util.HashSet<>();
        
        // 添加墙壁
        obstacles.addAll(gameState.getWalls());
        
        // 添加玩家蛇身
        obstacles.addAll(gameState.getBody());
        
        // 添加其他敌人蛇身
        for (EnemySnake enemy : gameState.getEnemies()) {
            if (enemy != this && enemy.isAlive()) {
                obstacles.addAll(enemy.getBody());
            }
        }
        
        return obstacles;
    }
    
    /**
     * 寻找安全目标点（用于防御策略）
     */
    private Point findSafeTarget(SnakeModel gameState, Set<Point> obstacles) {
        Point playerHead = gameState.getBody().peekFirst();
        if (playerHead == null) return null;
        
        Point currentPos = body.peekFirst();
        Point bestTarget = null;
        double maxDistance = 0;
        
        // 在地图边缘寻找远离玩家的点
        for (int x = 0; x < gameState.getCols(); x += 5) {
            for (int y = 0; y < gameState.getRows(); y += 5) {
                Point candidate = new Point(x, y);
                if (!obstacles.contains(candidate)) {
                    double distance = playerHead.distance(candidate);
                    if (distance > maxDistance) {
                        maxDistance = distance;
                        bestTarget = candidate;
                    }
                }
            }
        }
        
        return bestTarget;
    }

    /**
     * 智能选择下一个方向
     */
    private Direction chooseNextDirection(SnakeModel gameState) {
        // 如果有计算好的路径，优先使用
        if (!currentPath.isEmpty()) {
            Direction pathDirection = PathFinder.getNextDirection(body.peekFirst(), currentPath);
            if (pathDirection != null && isSafeDirection(pathDirection, gameState)) {
                return pathDirection;
            }
        }
        
        // 如果没有路径或路径不安全，使用备用策略
        return chooseSafeDirection(gameState);
    }
    
    /**
     * 选择安全方向（备用策略）
     */
    private Direction chooseSafeDirection(SnakeModel gameState) {
        List<Direction> possibleDirections = new ArrayList<>();
        Collections.addAll(possibleDirections, Direction.values());
        
        // 移除相反方向，防止掉头
        possibleDirections.remove(direction.getOpposite());

        List<Direction> safeDirections = new ArrayList<>();
        for (Direction dir : possibleDirections) {
            if (isSafeDirection(dir, gameState)) {
                safeDirections.add(dir);
            }
        }
        
        // 如果有安全的路径，根据策略选择
        if (!safeDirections.isEmpty()) {
            switch (strategy) {
                case AGGRESSIVE:
                    // 选择最接近玩家的方向
                    return chooseDirectionTowardsPlayer(safeDirections, gameState);
                case DEFENSIVE:
                    // 选择远离玩家的方向
                    return chooseDirectionAwayFromPlayer(safeDirections, gameState);
                case FOOD_HUNTER:
                    // 选择朝向食物的方向
                    return chooseDirectionTowardsFood(safeDirections, gameState);
                case RANDOM:
                default:
                    return safeDirections.get(random.nextInt(safeDirections.size()));
            }
        }

        // 如果没有安全路径，就继续走当前方向
        return this.direction;
    }
    
    /**
     * 选择朝向玩家的方向
     */
    private Direction chooseDirectionTowardsPlayer(List<Direction> safeDirections, SnakeModel gameState) {
        Point playerHead = gameState.getBody().peekFirst();
        if (playerHead == null) return safeDirections.get(0);
        
        Point currentPos = body.peekFirst();
        Direction bestDirection = safeDirections.get(0);
        double minDistance = Double.MAX_VALUE;
        
        for (Direction dir : safeDirections) {
            Point nextPos = new Point(currentPos.x + dir.dx, currentPos.y + dir.dy);
            nextPos = wrap(nextPos, gameState.getCols(), gameState.getRows());
            double distance = nextPos.distance(playerHead);
            if (distance < minDistance) {
                minDistance = distance;
                bestDirection = dir;
            }
        }
        
        return bestDirection;
    }
    
    /**
     * 选择远离玩家的方向
     */
    private Direction chooseDirectionAwayFromPlayer(List<Direction> safeDirections, SnakeModel gameState) {
        Point playerHead = gameState.getBody().peekFirst();
        if (playerHead == null) return safeDirections.get(0);
        
        Point currentPos = body.peekFirst();
        Direction bestDirection = safeDirections.get(0);
        double maxDistance = 0;
        
        for (Direction dir : safeDirections) {
            Point nextPos = new Point(currentPos.x + dir.dx, currentPos.y + dir.dy);
            nextPos = wrap(nextPos, gameState.getCols(), gameState.getRows());
            double distance = nextPos.distance(playerHead);
            if (distance > maxDistance) {
                maxDistance = distance;
                bestDirection = dir;
            }
        }
        
        return bestDirection;
    }
    
    /**
     * 选择朝向食物的方向
     */
    private Direction chooseDirectionTowardsFood(List<Direction> safeDirections, SnakeModel gameState) {
        if (gameState.getFruit() == null || gameState.getFruit().cells.isEmpty()) {
            return safeDirections.get(0);
        }
        
        Point foodPos = gameState.getFruit().cells.get(0);
        Point currentPos = body.peekFirst();
        Direction bestDirection = safeDirections.get(0);
        double minDistance = Double.MAX_VALUE;
        
        for (Direction dir : safeDirections) {
            Point nextPos = new Point(currentPos.x + dir.dx, currentPos.y + dir.dy);
            nextPos = wrap(nextPos, gameState.getCols(), gameState.getRows());
            double distance = nextPos.distance(foodPos);
            if (distance < minDistance) {
                minDistance = distance;
                bestDirection = dir;
            }
        }
        
        return bestDirection;
    }

    /**
     * 检查方向是否安全
     */
    private boolean isSafeDirection(Direction dir, SnakeModel gameState) {
        Point nextPos = new Point(body.peekFirst());
        nextPos.translate(dir.dx, dir.dy);
        nextPos = wrap(nextPos, gameState.getCols(), gameState.getRows());
        return isSafe(nextPos, gameState);
    }

    /**
     * 检查目标位置是否安全
     */
    private boolean isSafe(Point pos, SnakeModel gameState) {
        // 1. 不能撞墙
        if (gameState.getWalls().contains(pos)) return false;
        // 2. 不能撞玩家
        if (gameState.getBody().contains(pos)) return false;
        // 3. 不能撞自己
        if (body.contains(pos)) return false;
        // 4. 不能撞其他敌人
        for (EnemySnake other : gameState.getEnemies()) {
            if (other != this && other.getBody().contains(pos)) {
                return false;
            }
        }
        return true;
    }

    private Point wrap(Point p, int cols, int rows){
        return new Point((p.x + cols) % cols, (p.y + rows) % rows);
    }
    
    // Getters and Setters
    public ArrayDeque<Point> getBody() {
        return body;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }
    
    public AIStrategy getStrategy() {
        return strategy;
    }

    public void grow(int amount) {
        this.growth += amount;
    }
} 