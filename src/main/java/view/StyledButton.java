package snake.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import snake.util.Constants;
import snake.sound.SoundManager;
import snake.sound.SoundClip;

public class StyledButton extends JButton {
    private Color originalColor = Constants.COLOR_THEME_BUTTON;
    private Color hoverColor = Constants.COLOR_THEME_BUTTON_HOVER;
    private boolean isHovered = false;

    public StyledButton(String text) {
        super(text);
        setFont(Constants.FONT_UI_DEFAULT);
        setForeground(Constants.COLOR_THEME_TEXT);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SoundManager.getInstance().playSound(SoundClip.CLICK);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (isHovered) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(originalColor);
        }
        
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        
        g2.setColor(getForeground());
        FontMetrics metrics = g2.getFontMetrics(getFont());
        int x = (getWidth() - metrics.stringWidth(getText())) / 2;
        int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
        g2.drawString(getText(), x, y);
        
        g2.dispose();
    }
} 