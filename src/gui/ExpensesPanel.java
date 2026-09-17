package gui;

import dao.ExpenseDAO;
import model.Expense;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ExpensesPanel extends JPanel implements Refreshable {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Category", "Amount (Rs.)", "Description", "Date"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JRadioButton allTimeBtn = new JRadioButton("All Time", true);
    private final JRadioButton thisMonthBtn = new JRadioButton("This Month");
    private final JLabel totalLabel = new JLabel(" ");

    public ExpensesPanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JLabel pageTitle = new JLabel("Your Expenses");
        pageTitle.setFont(Theme.FONT_TITLE);
        pageTitle.setForeground(Theme.TEXT_DARK);

        allTimeBtn.setOpaque(false);
        thisMonthBtn.setOpaque(false);
        allTimeBtn.setFont(Theme.FONT_BODY);
        thisMonthBtn.setFont(Theme.FONT_BODY);
        ButtonGroup group = new ButtonGroup();
        group.add(allTimeBtn);
        group.add(thisMonthBtn);
        allTimeBtn.addActionListener(e -> refreshData());
        thisMonthBtn.addActionListener(e -> refreshData());

        RoundedButton deleteBtn = new RoundedButton("Delete Selected", Theme.DANGER, new Color(0x8E0000));
        deleteBtn.addActionListener(e -> deleteSelected());

        RoundedButton refreshBtn = new RoundedButton("Refresh", Theme.ACCENT, Theme.PRIMARY_DARK);
        refreshBtn.addActionListener(e -> refreshData());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        topBar.setOpaque(false);
        topBar.add(allTimeBtn);
        topBar.add(thisMonthBtn);
        topBar.add(refreshBtn);
        topBar.add(deleteBtn);

        JPanel headerBlock = new JPanel();
        headerBlock.setOpaque(false);
        headerBlock.setLayout(new BoxLayout(headerBlock, BoxLayout.Y_AXIS));
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerBlock.add(pageTitle);
        headerBlock.add(Box.createVerticalStrut(14));
        headerBlock.add(topBar);

        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.setFont(Theme.FONT_BODY);
        table.setSelectionBackground(new Color(0xC8E6C9));
        table.setSelectionForeground(Theme.TEXT_DARK);
        table.setGridColor(new Color(0xE0E0E0));
        table.setShowVerticalLines(false);
        table.setRowSelectionAllowed(true);

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setFont(Theme.FONT_BODY_BOLD);
        tableHeader.setBackground(Theme.BACKGROUND);
        tableHeader.setForeground(Theme.TEXT_MUTED);

        RoundedPanel tableCard = new RoundedPanel(18, Theme.CARD_BG);
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        totalLabel.setFont(Theme.FONT_HEADING);
        totalLabel.setForeground(Theme.PRIMARY);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(12, 4, 0, 4));

        tableCard.add(scrollPane, BorderLayout.CENTER);
        tableCard.add(totalLabel, BorderLayout.SOUTH);

        add(headerBlock, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        try {
            List<Expense> expenses = thisMonthBtn.isSelected()
                    ? expenseDAO.getExpensesForCurrentMonth()
                    : expenseDAO.getAllExpenses();

            tableModel.setRowCount(0);
            double total = 0;
            for (Expense e : expenses) {
                tableModel.addRow(new Object[]{
                        e.getExpenseId(), e.getCategoryName(),
                        String.format("%.2f", e.getAmount()),
                        e.getDescription(), e.getDate().toString()
                });
                total += e.getAmount();
            }
            totalLabel.setText(String.format("Total: Rs.%,.2f   (%d expenses)", total, expenses.size()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading expenses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete expense #" + id + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            expenseDAO.deleteExpense(id);
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
