package snake.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private static EventManager instance;
    private final Map<GameEvent.EventType, List<GameEventListener>> listeners;
    
    private EventManager() {
        listeners = new ConcurrentHashMap<>();
    }
    
    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }
    
    public void addEventListener(GameEvent.EventType type, GameEventListener listener) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(listener);
    }
    
    public void removeEventListener(GameEvent.EventType type, GameEventListener listener) {
        List<GameEventListener> typeListeners = listeners.get(type);
        if (typeListeners != null) {
            typeListeners.remove(listener);
        }
    }
    
    public void fireEvent(GameEvent event) {
        List<GameEventListener> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            for (GameEventListener listener : typeListeners) {
                try {
                    listener.onGameEvent(event);
                } catch (Exception e) {
                    System.err.println("Error in event listener: " + e.getMessage());
                }
            }
        }
    }
    
    public void fireEvent(Object source, GameEvent.EventType type) {
        fireEvent(new GameEvent(source, type));
    }
    
    public void fireEvent(Object source, GameEvent.EventType type, Object data) {
        fireEvent(new GameEvent(source, type, data));
    }
    
    public void clear() {
        listeners.clear();
    }
} 