package snake.sound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import snake.util.GameConfig;
import snake.util.GameEvent;
import snake.util.GameEventListener;

public class SoundManager implements GameEventListener {

    private static SoundManager instance;
    private final EnumMap<SoundClip, Clip> clips;
    private boolean isEnabled = true;
    private float volume = 0.7f;

    private SoundManager() {
        clips = new EnumMap<>(SoundClip.class);
        GameConfig config = GameConfig.getInstance();
        this.isEnabled = config.isSoundEnabled();
        this.volume = config.getSoundVolume();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public void loadSounds() {
        for (SoundClip sound : SoundClip.values()) {
            try {
                URL url = getClass().getResource("/sounds/" + sound.getFileName());
                if (url == null) {
                    System.err.println("Could not find sound file: " + sound.getFileName());
                    continue;
                }
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                setClipVolume(clip);
                clips.put(sound, clip);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Error loading sound: " + sound.getFileName());
                e.printStackTrace();
            }
        }
    }
    
    public void playSound(SoundClip sound) {
        if (!isEnabled || !clips.containsKey(sound)) {
            return;
        }
        Clip clip = clips.get(sound);
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    private void setClipVolume(Clip clip) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = gainControl.getMaximum() - gainControl.getMinimum();
            float gain = (range * volume) + gainControl.getMinimum();
            gainControl.setValue(gain);
        }
    }
    
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        GameConfig.getInstance().setSoundEnabled(enabled);
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        clips.values().forEach(this::setClipVolume);
        GameConfig.getInstance().setSoundVolume(this.volume);
    }

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.getType()) {
            case FOOD_EATEN:
                playSound(SoundClip.EAT);
                break;
            case GAME_OVER:
                playSound(SoundClip.GAME_OVER);
                break;
            default:
                break;
        }
    }
} 