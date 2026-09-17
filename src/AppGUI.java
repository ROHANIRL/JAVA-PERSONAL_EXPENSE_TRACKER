import gui.MainFrame;

import javax.swing.*;

/** Entry point for the decorative Swing desktop version of the app. Run this instead of Main
 *  for the graphical interface; both share the exact same database layer. */
public class AppGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
