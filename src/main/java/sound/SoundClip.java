package snake.sound;

public enum SoundClip {
    EAT("eat.wav"),
    GAME_OVER("game_over.wav"),
    CLICK("click.wav");

    private final String fileName;

    SoundClip(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
} 