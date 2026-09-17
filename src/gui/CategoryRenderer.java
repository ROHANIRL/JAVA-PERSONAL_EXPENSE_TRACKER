package gui;

import model.Category;

import javax.swing.*;
import java.awt.*;

/** Shows just the category name in dropdowns, hiding the numeric ID from the user. */
public class CategoryRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Category c) {
            setText(c.getCategoryName());
        }
        return this;
    }
}
