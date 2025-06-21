package snake;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import snake.model.SnakeModel;
import snake.util.Constants;
import snake.util.EventManager;
import snake.util.GameEvent;

public final class SaveData {
    private static final String SAVE_DIR = "saves";
    private static final String SAVE_EXTENSION = ".sav";
    private static final String METADATA_EXTENSION = ".meta";
    
    // 存档元数据类
    public static class SaveMetadata implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String saveName;
        public final LocalDateTime saveTime;
        public final int score;
        public final int snakeLength;
        public final String checksum;
        
        public SaveMetadata(String name, LocalDateTime time, int score, int length, String checksum) {
            this.saveName = name;
            this.saveTime = time;
            this.score = score;
            this.snakeLength = length;
            this.checksum = checksum;
        }
        
        @Override
        public String toString() {
            return String.format("%s - Score: %d, Length: %d (%s)", 
                saveName, score, snakeLength, 
                saveTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
    }
    
    private SaveData() {}
    
    // 确保存档目录存在
    private static void ensureSaveDirectory() {
        Path savePath = Path.of(SAVE_DIR);
        if (!Files.exists(savePath)) {
            try {
                Files.createDirectories(savePath);
            } catch (IOException e) {
                System.err.println("Failed to create save directory: " + e.getMessage());
            }
        }
    }
    
    // 计算数据的MD5校验和
    private static String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "unknown";
        }
    }
    
    // 压缩数据
    private static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(data);
        }
        return baos.toByteArray();
    }
    
    // 解压数据
    private static byte[] decompress(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(bais)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        }
        return baos.toByteArray();
    }
    
    // 保存游戏到指定槽位
    public static boolean save(SnakeModel model, int slot) {
        return save(model, "Slot " + slot, slot);
    }
    
    // 保存游戏到指定槽位和名称
    public static boolean save(SnakeModel model, String saveName, int slot) {
        ensureSaveDirectory();
        
        try {
            // 序列化模型
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(model);
            }
            byte[] modelData = baos.toByteArray();
            
            // 压缩数据
            byte[] compressedData = compress(modelData);
            
            // 计算校验和
            String checksum = calculateChecksum(modelData);
            
            // 保存游戏数据
            Path savePath = Path.of(SAVE_DIR, "save_" + slot + SAVE_EXTENSION);
            Files.write(savePath, compressedData);
            
            // 保存元数据
            SaveMetadata metadata = new SaveMetadata(
                saveName,
                LocalDateTime.now(),
                model.getScore(),
                model.getBody().size(),
                checksum
            );
            
            Path metaPath = Path.of(SAVE_DIR, "save_" + slot + METADATA_EXTENSION);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(metaPath))) {
                oos.writeObject(metadata);
            }
            
            // 触发存档事件
            EventManager.getInstance().fireEvent(SaveData.class, GameEvent.EventType.SAVE_GAME, slot);
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
            return false;
        }
    }
    
    // 从指定槽位加载游戏
    public static SnakeModel load(int slot) {
        ensureSaveDirectory();
        
        try {
            Path savePath = Path.of(SAVE_DIR, "save_" + slot + SAVE_EXTENSION);
            if (!Files.exists(savePath)) {
                return null;
            }
            
            // 读取压缩数据
            byte[] compressedData = Files.readAllBytes(savePath);
            
            // 解压数据
            byte[] modelData = decompress(compressedData);
            
            // 验证校验和
            Path metaPath = Path.of(SAVE_DIR, "save_" + slot + METADATA_EXTENSION);
            if (Files.exists(metaPath)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(metaPath))) {
                    SaveMetadata metadata = (SaveMetadata) ois.readObject();
                    String currentChecksum = calculateChecksum(modelData);
                    if (!currentChecksum.equals(metadata.checksum)) {
                        System.err.println("Save file corrupted: checksum mismatch");
                        return null;
                    }
                }
            }
            
            // 反序列化模型
            try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(modelData))) {
                SnakeModel model = (SnakeModel) ois.readObject();
                
                // 触发加载事件
                EventManager.getInstance().fireEvent(SaveData.class, GameEvent.EventType.LOAD_GAME, slot);
                
                return model;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            return null;
        }
    }
    
    // 获取存档元数据
    public static SaveMetadata getMetadata(int slot) {
        ensureSaveDirectory();
        
        Path metaPath = Path.of(SAVE_DIR, "save_" + slot + METADATA_EXTENSION);
        if (!Files.exists(metaPath)) {
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(metaPath))) {
            return (SaveMetadata) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load metadata: " + e.getMessage());
            return null;
        }
    }
    
    // 获取所有存档信息
    public static List<SaveMetadata> getAllSaves() {
        List<SaveMetadata> saves = new ArrayList<>();
        for (int i = 0; i < Constants.MAX_SAVE_SLOTS; i++) {
            SaveMetadata metadata = getMetadata(i);
            if (metadata != null) {
                saves.add(metadata);
            }
        }
        return saves;
    }
    
    // 删除指定槽位的存档
    public static boolean delete(int slot) {
        ensureSaveDirectory();
        
        Path savePath = Path.of(SAVE_DIR, "save_" + slot + SAVE_EXTENSION);
        Path metaPath = Path.of(SAVE_DIR, "save_" + slot + METADATA_EXTENSION);
        
        boolean success = true;
        try {
            Files.deleteIfExists(savePath);
            Files.deleteIfExists(metaPath);
        } catch (IOException e) {
            System.err.println("Failed to delete save: " + e.getMessage());
            success = false;
        }
        
        return success;
    }
    
    // 检查指定槽位是否有存档
    public static boolean exists(int slot) {
        ensureSaveDirectory();
        Path savePath = Path.of(SAVE_DIR, "save_" + slot + SAVE_EXTENSION);
        return Files.exists(savePath);
    }
    
    // 自动存档
    public static boolean autoSave(SnakeModel model) {
        return save(model, "Auto Save", 0);
    }
    
    // 兼容旧版本的读取方法
    public static SnakeModel read() {
        return load(0); // 默认读取槽位0
    }
    
    // 兼容旧版本的写入方法
    public static void write(SnakeModel model) {
        save(model, 0);
    }
    
    // 兼容旧版本的检查方法
    public static boolean exists() {
        return exists(0);
    }
    
    // 兼容旧版本的删除方法
    public static void delete() {
        delete(0);
    }
}