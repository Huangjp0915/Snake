package snake.util;

@FunctionalInterface
public interface GameEventListener {
    void onGameEvent(GameEvent event);
} 