package src.main.java.model;

import java.io.Serializable;
import src.main.java.util.Direction;
import src.main.java.util.GameState;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Random;

public class SnakeModel implements Serializable{
    private static final long serialVersionUID = 1L;
    private final int cols, rows;                 // 逻辑坐标系
    private final Random rnd = new Random();

    private ArrayDeque<Point> snake = new ArrayDeque<>();
    private Point   food;
    private Direction dir  = Direction.RIGHT;
    private GameState state = GameState.RUNNING;
    private int score = 0;

    public SnakeModel(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        reset();
    }

    /* ===== 核心逻辑 ===== */
    public void reset() {
        snake.clear();
        snake.add(new Point(cols / 2, rows / 2));
        dir   = Direction.RIGHT;
        score = 0;
        state = GameState.RUNNING;
        spawnFood();
    }

    public void tick() {
        if (state != GameState.RUNNING) return;

        Point head = snake.peekFirst();
        Point next = new Point((head.x + dir.dx + cols) % cols,
                               (head.y + dir.dy + rows) % rows);

        if (snake.contains(next)) { state = GameState.GAME_OVER; return; }

        snake.addFirst(next);

        if (next.equals(food)) {
            score++;
            spawnFood();
        } else {
            snake.removeLast();
        }
    }

    public void turn(Direction d) { if (!d.isOpposite(dir)) dir = d; }

    private void spawnFood() {
        do {
            food = new Point(rnd.nextInt(cols), rnd.nextInt(rows));
        } while (snake.contains(food));
    }

    /* ====== Getters ====== */
    public ArrayDeque<Point> getSnake()  { return snake; }
    public Point             getFood()   { return food; }
    public GameState         getState()  { return state; }
    public int               getScore()  { return score; }
    public int               getCols()   { return cols; }
    public int               getRows()   { return rows; }

    /* ====== 状态机 ====== */
    public void setState(GameState s) { this.state = s; }
}