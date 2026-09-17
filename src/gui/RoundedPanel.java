package gui;

import javax.swing.*;
import java.awt.*;

/** A JPanel that paints itself as a rounded, flat-colored "card". */
public class RoundedPanel extends JPanel {
    private final int arc;
    private final Color bgColor;

    public RoundedPanel(int arc, Color bgColor) {
        this.arc = arc;
        this.bgColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        g2.dispose();
        super.paintComponent(g);
    }
}
