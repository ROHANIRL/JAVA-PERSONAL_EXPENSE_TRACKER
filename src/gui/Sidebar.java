package gui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/** Left-hand navigation rail with icon+label buttons for each section. */
public class Sidebar extends JPanel {

    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public Sidebar(String[][] items, Consumer<String> onSelect) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.SIDEBAR_BG);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        setPreferredSize(new Dimension(210, 0));

        JLabel logo = new JLabel("\u20B9  ExpenseTracker");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logo.setForeground(Color.WHITE);
        logo.setBorder(BorderFactory.createEmptyBorder(28, 20, 32, 10));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(logo);

        for (String[] item : items) {
            String key = item[0];
            String icon = item[1];
            String label = item[2];

            JButton btn = new JButton("  " + icon + "     " + label);
            btn.setFont(Theme.FONT_NAV);
            btn.setForeground(Theme.SIDEBAR_TEXT);
            btn.setBackground(Theme.SIDEBAR_BG);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                setActive(key);
                onSelect.accept(key);
            });

            navButtons.put(key, btn);
            add(btn);
        }

        add(Box.createVerticalGlue());

        if (items.length > 0) {
            setActive(items[0][0]);
        }
    }

    private void setActive(String key) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            entry.getValue().setBackground(active ? Theme.SIDEBAR_HOVER : Theme.SIDEBAR_BG);
        }
    }
}
