package gui;

import dao.BudgetDAO;
import dao.CategoryDAO;
import model.Budget;
import model.Category;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class BudgetsPanel extends JPanel implements Refreshable {

    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Category", "Spent (Rs.)", "Limit (Rs.)", "% Used", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JComboBox<Category> categoryBox = new JComboBox<>();
    private final JTextField limitField = new JTextField(10);
    private final JLabel statusLabel = new JLabel(" ");

    private Runnable onBudgetChanged;

    public BudgetsPanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JLabel pageTitle = new JLabel("Category Budgets");
        pageTitle.setFont(Theme.FONT_TITLE);
        pageTitle.setForeground(Theme.TEXT_DARK);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        categoryBox.setRenderer(new CategoryRenderer());
        categoryBox.setFont(Theme.FONT_BODY);
        limitField.setFont(Theme.FONT_BODY);

        RoundedButton setBtn = new RoundedButton("Set Budget", Theme.PRIMARY, Theme.PRIMARY_DARK);
        setBtn.addActionListener(e -> setBudget());

        RoundedButton refreshBtn = new RoundedButton("Refresh", Theme.ACCENT, Theme.PRIMARY_DARK);
        refreshBtn.addActionListener(e -> refreshData());

        RoundedPanel formCard = new RoundedPanel(18, Theme.CARD_BG);
        formCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        formCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(Theme.FONT_BODY_BOLD);
        JLabel limitLabel = new JLabel("Monthly Limit (Rs.):");
        limitLabel.setFont(Theme.FONT_BODY_BOLD);

        formCard.add(catLabel);
        formCard.add(categoryBox);
        formCard.add(limitLabel);
        formCard.add(limitField);
        formCard.add(setBtn);
        formCard.add(refreshBtn);

        statusLabel.setFont(Theme.FONT_BODY_BOLD);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerBlock = new JPanel();
        headerBlock.setOpaque(false);
        headerBlock.setLayout(new BoxLayout(headerBlock, BoxLayout.Y_AXIS));
        headerBlock.add(pageTitle);
        headerBlock.add(Box.createVerticalStrut(14));
        headerBlock.add(formCard);
        headerBlock.add(Box.createVerticalStrut(8));
        headerBlock.add(statusLabel);

        table.setRowHeight(30);
        table.setFont(Theme.FONT_BODY);
        table.setSelectionBackground(new Color(0xC8E6C9));
        table.setGridColor(new Color(0xE0E0E0));
        table.setShowVerticalLines(false);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(Theme.FONT_BODY_BOLD);
        tableHeader.setBackground(Theme.BACKGROUND);
        tableHeader.setForeground(Theme.TEXT_MUTED);

        RoundedPanel tableCard = new RoundedPanel(18, Theme.CARD_BG);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(headerBlock, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    public void setOnBudgetChanged(Runnable callback) {
        this.onBudgetChanged = callback;
    }

    @Override
    public void refreshData() {
        try {
            Category selected = (Category) categoryBox.getSelectedItem();
            List<Category> categories = categoryDAO.getAllCategories();
            categoryBox.setModel(new DefaultComboBoxModel<>(new Vector<>(categories)));
            if (selected != null) {
                for (Category c : categories) {
                    if (c.getCategoryId() == selected.getCategoryId()) {
                        categoryBox.setSelectedItem(c);
                        break;
                    }
                }
            }

            List<Budget> budgets = budgetDAO.getAllBudgetsWithProgress();
            tableModel.setRowCount(0);
            for (Budget b : budgets) {
                String status = b.getPercentUsed() >= 100 ? "OVER BUDGET"
                        : b.getPercentUsed() >= 80 ? "WARNING" : "OK";
                tableModel.addRow(new Object[]{
                        b.getCategoryName(),
                        String.format("%.2f", b.getSpentSoFar()),
                        String.format("%.2f", b.getMonthlyLimit()),
                        String.format("%.0f%%", b.getPercentUsed()),
                        status
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading budgets: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setBudget() {
        Category category = (Category) categoryBox.getSelectedItem();
        if (category == null) {
            statusLabel.setText("Add a category first.");
            return;
        }
        double limit;
        try {
            limit = Double.parseDouble(limitField.getText().trim());
            if (limit <= 0) throw new NumberFormatException();
        } catch (NumberFormatException nfe) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Enter a valid positive limit.");
            return;
        }
        try {
            budgetDAO.setBudget(category.getCategoryId(), limit);
            statusLabel.setForeground(Theme.PRIMARY);
            statusLabel.setText("Budget set for " + category.getCategoryName() + ".");
            limitField.setText("");
            refreshData();
            if (onBudgetChanged != null) onBudgetChanged.run();
        } catch (SQLException ex) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Database error: " + ex.getMessage());
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String status = String.valueOf(value);
            if (!isSelected) {
                switch (status) {
                    case "OVER BUDGET" -> c.setForeground(Theme.DANGER);
                    case "WARNING" -> c.setForeground(Theme.WARNING);
                    default -> c.setForeground(Theme.PRIMARY);
                }
            }
            setFont(getFont().deriveFont(Font.BOLD));
            return c;
        }
    }
}
