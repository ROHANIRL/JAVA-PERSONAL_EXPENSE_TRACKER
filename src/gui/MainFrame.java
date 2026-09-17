package gui;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Personal Expense Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 640);
        setMinimumSize(new Dimension(840, 520));
        setLocationRelativeTo(null);

        DashboardPanel dashboardPanel = new DashboardPanel();
        AddExpensePanel addPanel = new AddExpensePanel();
        ExpensesPanel expensesPanel = new ExpensesPanel();
        BudgetsPanel budgetsPanel = new BudgetsPanel();
        SavingsPanel savingsPanel = new SavingsPanel();

        addPanel.setOnExpenseAdded(() -> {
            dashboardPanel.refreshData();
            expensesPanel.refreshData();
            budgetsPanel.refreshData();
            savingsPanel.refreshData();
        });
        budgetsPanel.setOnBudgetChanged(() -> {
            dashboardPanel.refreshData();
            expensesPanel.refreshData();
        });

        Map<String, Refreshable> panelsByKey = new LinkedHashMap<>();
        panelsByKey.put("dashboard", dashboardPanel);
        panelsByKey.put("add", addPanel);
        panelsByKey.put("expenses", expensesPanel);
        panelsByKey.put("budgets", budgetsPanel);
        panelsByKey.put("savings", savingsPanel);

        CardLayout cardLayout = new CardLayout();
        JPanel content = new JPanel(cardLayout);
        content.setBackground(Theme.BACKGROUND);

        content.add(wrapScrollable(dashboardPanel), "dashboard");
        content.add(wrapScrollable(addPanel), "add");
        content.add(expensesPanel, "expenses");
        content.add(budgetsPanel, "budgets");
        content.add(wrapScrollable(savingsPanel), "savings");

        String[][] navItems = {
                {"dashboard", "\u25C6", "Dashboard"},
                {"add", "+", "Add Expense"},
                {"expenses", "\u2261", "Expenses"},
                {"budgets", "\u25A3", "Budgets"},
                {"savings", "\u20B9", "Savings"}
        };

        Sidebar sidebar = new Sidebar(navItems, key -> {
            cardLayout.show(content, key);
            Refreshable panel = panelsByKey.get(key);
            if (panel != null) panel.refreshData();
        });

        setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        dashboardPanel.refreshData();
        expensesPanel.refreshData();
        budgetsPanel.refreshData();
        savingsPanel.refreshData();
    }

    private JScrollPane wrapScrollable(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }
}
