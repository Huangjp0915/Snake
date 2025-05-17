package src.main.java;
import javax.swing.*;

import src.main.java.model.SnakeModel;

import java.awt.*;
import java.awt.event.*;

public class MainMenu {
    private static SnakeGameWindow currentWindow = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainMenu::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Snake Main Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(320, 260);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Color.BLACK);

        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        JButton btnNew      = darkButton("New Game");
        JButton btnContinue = darkButton("Continue Game");
        JButton btnQuit     = darkButton("Quit Game");

        btnNew.addActionListener(e -> { SaveData.delete(); openSizeDialog(frame); });
        btnContinue.addActionListener(e -> handleContinue());
        btnQuit.addActionListener(e -> System.exit(0));

        panel.add(btnNew);
        panel.add(Box.createVerticalStrut(18));
        panel.add(btnContinue);
        panel.add(Box.createVerticalStrut(18));
        panel.add(btnQuit);

        frame.setContentPane(panel);
        frame.setVisible(true);

        frame.addWindowFocusListener(new WindowAdapter() {
            @Override public void windowGainedFocus(WindowEvent e) { updateContinueEnabled(btnContinue); }
        });
        updateContinueEnabled(btnContinue);
    }

    private static JButton darkButton(String text) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(180, 36));
        b.setBackground(Color.BLACK);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Consolas", Font.BOLD, 18));
        b.setFocusPainted(false);
        return b;
    }

    /* ---------- Continue Game ---------- */
    private static void handleContinue() {
        if (currentWindow != null && currentWindow.isRunning()) {
            currentWindow.bringToFront();
            return;
        }
        SnakeModel m = SaveData.read();
        if (m != null) currentWindow = new SnakeGameWindow(m);
    }

    private static void updateContinueEnabled(JButton btn) {
        boolean enable = (currentWindow != null && currentWindow.isRunning()) || SaveData.exists();
        btn.setEnabled(enable);
    }

    /* ---------- 新建游戏 ---------- */
    private static void openSizeDialog(JFrame owner) {
        JDialog dlg = new JDialog(owner, "Select Size", true);
        dlg.setSize(260, 200);
        dlg.setLocationRelativeTo(owner);
        dlg.setLayout(new GridLayout(3, 1, 10, 10));
        dlg.getContentPane().setBackground(Color.BLACK);

        JButton small  = darkButton("Small 400 x 400");
        JButton medium = darkButton("Medium 600 x 600");
        JButton large  = darkButton("Large 800 x 800");

        small.addActionListener(e -> { dlg.dispose(); openGame(400, 400); });
        medium.addActionListener(e -> { dlg.dispose(); openGame(600, 600); });
        large.addActionListener(e -> { dlg.dispose(); openGame(800, 800); });

        dlg.add(small);
        dlg.add(medium);
        dlg.add(large);
        dlg.setVisible(true);
    }

    private static void openGame(int w, int h) {
        if (currentWindow != null && currentWindow.isRunning()) currentWindow.dispose();
        currentWindow = new SnakeGameWindow(w, h);
    }
}