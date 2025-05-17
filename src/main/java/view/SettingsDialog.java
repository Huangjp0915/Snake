package src.main.java.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

import src.main.java.model.SnakeModel;
import src.main.java.controller.SnakeController;
import src.main.java.SaveData;

public class SettingsDialog extends JDialog {
    public SettingsDialog(JFrame owner,
                          SnakeModel model,
                          SnakeController ctl) {
        super(owner, "Settings", true);              // 模态对话框
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLayout(new GridLayout(3,1,0,10));
        setPreferredSize(new Dimension(200, 180));
        Font f = new Font("Consolas", Font.BOLD, 16);

        JButton btnContinue = new JButton("resume game");
        JButton btnRestart  = new JButton("restart game");
        JButton btnSaveQuit = new JButton("save and quit");
        for (JButton b : new JButton[]{btnContinue,btnRestart,btnSaveQuit}) {
            b.setFont(f);
            add(b);
        }

        btnContinue.addActionListener(e -> {
            setVisible(false);
            ctl.resume();              // ← 恢复计时器
        });

        btnRestart.addActionListener(e -> {
            model.reset();
            setVisible(false);
            ctl.resume();
        });

        btnSaveQuit.addActionListener(e -> {
            SaveData.write(model);
            owner.dispose();           // 触发 SnakeGameWindow.windowClosed → ctl.shutdown()
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                ctl.resume();
            }
        });

        pack();
    }
}
