package src.main.java;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import src.main.java.model.SnakeModel;

public final class SaveData {
    private static final Path SAVE = Path.of("snake_save.dat");   // 后缀随意

    private SaveData() {}

    public static void write(SnakeModel m) {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(Files.newOutputStream(SAVE))) {
            oos.writeObject(m);                    // 一行搞定
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static SnakeModel read() {
        if (!Files.exists(SAVE)) return null;
        try (ObjectInputStream ois =
                     new ObjectInputStream(Files.newInputStream(SAVE))) {
            return (SnakeModel) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace(); return null;
        }
    }

    public static boolean exists() { return Files.exists(SAVE); }
    public static void delete()    { try { Files.deleteIfExists(SAVE); } catch (IOException ignored) {} }
}