package gui;

import dao.CategoryDAO;
import dao.ExpenseDAO;
import model.Category;
import model.Expense;
import service.BudgetService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;

public class AddExpensePanel extends JPanel implements Refreshable {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final BudgetService budgetService = new BudgetService();

    private final JComboBox<Category> categoryBox = new JComboBox<>();
    private final JTextField amountField = new JTextField(18);
    private final JTextField descriptionField = new JTextField(18);
    private final JTextField dateField = new JTextField(LocalDate.now().toString(), 18);
    private final JLabel statusLabel = new JLabel(" ");

    private Runnable onExpenseAdded;

    public AddExpensePanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JLabel pageTitle = new JLabel("Add a New Expense");
        pageTitle.setFont(Theme.FONT_TITLE);
        pageTitle.setForeground(Theme.TEXT_DARK);
        pageTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        RoundedPanel card = new RoundedPanel(18, Theme.CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));

        categoryBox.setRenderer(new CategoryRenderer());
        categoryBox.setFont(Theme.FONT_BODY);
        amountField.setFont(Theme.FONT_BODY);
        descriptionField.setFont(Theme.FONT_BODY);
        dateField.setFont(Theme.FONT_BODY);

        RoundedButton addCategoryBtn = new RoundedButton("+ New Category", Theme.ACCENT, Theme.PRIMARY_DARK);
        addCategoryBtn.addActionListener(e -> addNewCategory());

        RoundedButton submitBtn = new RoundedButton("Add Expense", Theme.PRIMARY, Theme.PRIMARY_DARK);
        submitBtn.addActionListener(e -> submitExpense());

        card.add(formRow("Category", categoryBox, addCategoryBtn));
        card.add(Box.createVerticalStrut(16));
        card.add(formRow("Amount (Rs.)", amountField));
        card.add(Box.createVerticalStrut(16));
        card.add(formRow("Description", descriptionField));
        card.add(Box.createVerticalStrut(16));
        card.add(formRow("Date (YYYY-MM-DD)", dateField));
        card.add(Box.createVerticalStrut(24));

        JPanel submitRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        submitRow.setOpaque(false);
        submitRow.add(submitBtn);
        submitRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(submitRow);

        statusLabel.setFont(Theme.FONT_BODY_BOLD);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(pageTitle, BorderLayout.NORTH);
        centerWrap.add(card, BorderLayout.CENTER);

        add(centerWrap, BorderLayout.NORTH);

        refreshData();
    }

    private JPanel formRow(String labelText, JComponent... fields) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_BODY_BOLD);
        label.setForeground(Theme.TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        row.add(label);

        JPanel fieldsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        fieldsRow.setOpaque(false);
        fieldsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (JComponent field : fields) {
            fieldsRow.add(field);
        }
        row.add(fieldsRow);

        return row;
    }

    public void setOnExpenseAdded(Runnable callback) {
        this.onExpenseAdded = callback;
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
        } catch (SQLException e) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Could not load categories: " + e.getMessage());
        }
    }

    private void addNewCategory() {
        String name = JOptionPane.showInputDialog(this, "New category name:");
        if (name == null || name.isBlank()) return;
        try {
            categoryDAO.addCategory(name.trim());
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding category: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitExpense() {
        Category category = (Category) categoryBox.getSelectedItem();
        if (category == null) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Add a category first.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException nfe) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Enter a valid positive amount.");
            return;
        }
        LocalDate date;
        try {
            String dateText = dateField.getText().trim();
            date = dateText.isEmpty() ? LocalDate.now() : LocalDate.parse(dateText);
        } catch (Exception ex) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Date must be in YYYY-MM-DD format.");
            return;
        }

        try {
            Expense expense = new Expense(category.getCategoryId(), amount, descriptionField.getText().trim(), date);
            expenseDAO.addExpense(expense);

            statusLabel.setForeground(Theme.PRIMARY);
            statusLabel.setText("Expense added.");

            String alert = budgetService.checkBudgetAlert(category.getCategoryId());
            if (alert != null) {
                JOptionPane.showMessageDialog(this, alert, "Budget Alert", JOptionPane.WARNING_MESSAGE);
            }

            amountField.setText("");
            descriptionField.setText("");
            dateField.setText(LocalDate.now().toString());

            if (onExpenseAdded != null) onExpenseAdded.run();
        } catch (SQLException ex) {
            statusLabel.setForeground(Color.RED);
            statusLabel.setText("Database error: " + ex.getMessage());
        }
    }
}
