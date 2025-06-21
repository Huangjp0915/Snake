package snake.util;

import java.awt.Point;
import java.util.*;

/**
 * A*寻路算法实现
 * 用于敌人AI的智能寻路
 */
public class PathFinder {
    
    private static class Node implements Comparable<Node> {
        Point pos;
        Node parent;
        int g; // 从起点到当前点的代价
        int h; // 从当前点到目标的启发式代价
        int f; // f = g + h
        
        Node(Point pos, Node parent, int g, int h) {
            this.pos = pos;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f, other.f);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return Objects.equals(pos, node.pos);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }
    
    /**
     * 寻找从起点到目标的最短路径
     * @param start 起点
     * @param target 目标点
     * @param obstacles 障碍物集合（墙壁、蛇身等）
     * @param cols 地图宽度
     * @param rows 地图高度
     * @return 路径点列表，如果找不到路径则返回空列表
     */
    public static List<Point> findPath(Point start, Point target, Set<Point> obstacles, int cols, int rows) {
        if (start.equals(target)) {
            return Arrays.asList(start);
        }
        
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Point> closedSet = new HashSet<>();
        Map<Point, Node> allNodes = new HashMap<>();
        
        Node startNode = new Node(start, null, 0, heuristic(start, target));
        openSet.add(startNode);
        allNodes.put(start, startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            if (current.pos.equals(target)) {
                return reconstructPath(current);
            }
            
            closedSet.add(current.pos);
            
            // 检查四个方向的邻居
            for (Direction dir : Direction.values()) {
                Point neighborPos = new Point(current.pos.x + dir.dx, current.pos.y + dir.dy);
                
                // 边界检查和障碍物检查
                if (!isValidPosition(neighborPos, obstacles, cols, rows) || 
                    closedSet.contains(neighborPos)) {
                    continue;
                }
                
                int tentativeG = current.g + 1;
                Node neighbor = allNodes.get(neighborPos);
                
                if (neighbor == null) {
                    neighbor = new Node(neighborPos, current, tentativeG, heuristic(neighborPos, target));
                    allNodes.put(neighborPos, neighbor);
                    openSet.add(neighbor);
                } else if (tentativeG < neighbor.g) {
                    neighbor.parent = current;
                    neighbor.g = tentativeG;
                    neighbor.f = tentativeG + neighbor.h;
                }
            }
        }
        
        // 找不到路径
        return new ArrayList<>();
    }
    
    /**
     * 检查位置是否有效
     */
    private static boolean isValidPosition(Point pos, Set<Point> obstacles, int cols, int rows) {
        // 边界检查
        if (pos.x < 0 || pos.x >= cols || pos.y < 0 || pos.y >= rows) {
            return false;
        }
        // 障碍物检查
        return !obstacles.contains(pos);
    }
    
    /**
     * 曼哈顿距离启发式函数
     */
    private static int heuristic(Point from, Point to) {
        return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
    }
    
    /**
     * 重建路径
     */
    private static List<Point> reconstructPath(Node endNode) {
        List<Point> path = new ArrayList<>();
        Node current = endNode;
        
        while (current != null) {
            path.add(0, current.pos);
            current = current.parent;
        }
        
        return path;
    }
    
    /**
     * 获取下一步移动方向
     * @param current 当前位置
     * @param path 完整路径
     * @return 下一步应该移动的方向，如果路径为空则返回null
     */
    public static Direction getNextDirection(Point current, List<Point> path) {
        if (path.size() < 2) {
            return null;
        }
        
        // 找到当前点在路径中的位置
        int currentIndex = -1;
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).equals(current)) {
                currentIndex = i;
                break;
            }
        }
        
        if (currentIndex == -1 || currentIndex >= path.size() - 1) {
            return null;
        }
        
        Point next = path.get(currentIndex + 1);
        int dx = next.x - current.x;
        int dy = next.y - current.y;
        
        // 处理边界穿越
        if (Math.abs(dx) > 1) {
            dx = dx > 0 ? -1 : 1;
        }
        if (Math.abs(dy) > 1) {
            dy = dy > 0 ? -1 : 1;
        }
        
        // 根据差值确定方向
        if (dx == 1) return Direction.RIGHT;
        if (dx == -1) return Direction.LEFT;
        if (dy == 1) return Direction.DOWN;
        if (dy == -1) return Direction.UP;
        
        return null;
    }
} 