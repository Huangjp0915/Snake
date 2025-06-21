package snake.util;

import java.util.EventObject;

public class GameEvent extends EventObject {
    public enum EventType {
        GAME_START,
        GAME_PAUSE,
        GAME_RESUME,
        GAME_OVER,
        SCORE_CHANGED,
        FOOD_EATEN,
        WALL_SPAWNED,
        SNAKE_MOVED,
        SAVE_GAME,
        LOAD_GAME,
        ENEMY_SPAWNED
    }
    
    private final EventType type;
    private final Object data;
    
    public GameEvent(Object source, EventType type) {
        this(source, type, null);
    }
    
    public GameEvent(Object source, EventType type, Object data) {
        super(source);
        this.type = type;
        this.data = data;
    }
    
    public EventType getType() {
        return type;
    }
    
    public Object getData() {
        return data;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> clazz) {
        return clazz.isInstance(data) ? (T) data : null;
    }
} 