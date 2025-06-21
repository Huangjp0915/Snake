package snake.util;

import java.io.*;
import java.util.Properties;

public class GameConfig {
    private static final String CONFIG_FILE = "snake_config.properties";
    private static GameConfig instance;
    private Properties properties;
    
    // 默认配置
    private static final String DEFAULT_CONFIG = """
        # Snake Game Configuration
        game.speed=100
        sound.enabled=true
        sound.volume=0.7
        controls.up=UP
        controls.down=DOWN
        controls.left=LEFT
        controls.right=RIGHT
        controls.pause=ESCAPE
        controls.restart=SPACE
        theme.name=default
        window.size=medium
        auto.save=true
        auto.save.interval=30
        """;
    
    private GameConfig() {
        properties = new Properties();
        loadConfig();
    }
    
    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }
    
    private void loadConfig() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            createDefaultConfig();
        }
        
        try (FileInputStream fis = new FileInputStream(configFile)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
            loadDefaultProperties();
        }
    }
    
    private void createDefaultConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            fos.write(DEFAULT_CONFIG.getBytes());
        } catch (IOException e) {
            System.err.println("Failed to create default config: " + e.getMessage());
        }
    }
    
    private void loadDefaultProperties() {
        try (StringReader reader = new StringReader(DEFAULT_CONFIG)) {
            properties.load(reader);
        } catch (IOException e) {
            System.err.println("Failed to load default properties: " + e.getMessage());
        }
    }
    
    public void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Snake Game Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }
    
    // 获取配置值的方法
    public int getGameSpeed() {
        return Integer.parseInt(properties.getProperty("game.speed", "100"));
    }
    
    public void setGameSpeed(int speed) {
        properties.setProperty("game.speed", String.valueOf(speed));
        saveConfig();
    }
    
    public boolean isSoundEnabled() {
        return Boolean.parseBoolean(properties.getProperty("sound.enabled", "true"));
    }
    
    public void setSoundEnabled(boolean enabled) {
        properties.setProperty("sound.enabled", String.valueOf(enabled));
        saveConfig();
    }
    
    public float getSoundVolume() {
        return Float.parseFloat(properties.getProperty("sound.volume", "0.7"));
    }
    
    public void setSoundVolume(float volume) {
        properties.setProperty("sound.volume", String.valueOf(volume));
        saveConfig();
    }
    
    public String getControlKey(String action) {
        return properties.getProperty("controls." + action, "UNKNOWN");
    }
    
    public void setControlKey(String action, String key) {
        properties.setProperty("controls." + action, key);
        saveConfig();
    }
    
    public String getTheme() {
        return properties.getProperty("theme.name", "default");
    }
    
    public void setTheme(String theme) {
        properties.setProperty("theme.name", theme);
        saveConfig();
    }
    
    public String getWindowSize() {
        return properties.getProperty("window.size", "medium");
    }
    
    public void setWindowSize(String size) {
        properties.setProperty("window.size", size);
        saveConfig();
    }
    
    public boolean isAutoSaveEnabled() {
        return Boolean.parseBoolean(properties.getProperty("auto.save", "true"));
    }
    
    public void setAutoSaveEnabled(boolean enabled) {
        properties.setProperty("auto.save", String.valueOf(enabled));
        saveConfig();
    }
    
    public int getAutoSaveInterval() {
        return Integer.parseInt(properties.getProperty("auto.save.interval", "30"));
    }
    
    public void setAutoSaveInterval(int interval) {
        properties.setProperty("auto.save.interval", String.valueOf(interval));
        saveConfig();
    }
} 