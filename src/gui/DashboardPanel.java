package gui;

import dao.BudgetDAO;
import dao.ExpenseDAO;
import dao.UserProfileDAO;
import model.Budget;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Landing view: a quick decorative snapshot of this month's spending, savings, and budget health. */
public class DashboardPanel extends JPanel implements Refreshable {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final BudgetDAO budgetDAO = new BudgetDAO();
    private final UserProfileDAO profileDAO = new UserProfileDAO();

    private final JLabel spentValue = new JLabel("Rs.0");
    private final JLabel savingsValue = new JLabel("Rs.0");
    private final JLabel budgetValue = new JLabel("0 / 0");

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JLabel welcomeLabel = new JLabel("Welcome back!");
        welcomeLabel.setFont(Theme.FONT_TITLE);
        welcomeLabel.setForeground(Theme.TEXT_DARK);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Spend smart. Save more.");
        tagline.setFont(Theme.FONT_BODY);
        tagline.setForeground(Theme.TEXT_MUTED);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        tagline.setBorder(BorderFactory.createEmptyBorder(4, 0, 24, 0));

        JPanel cardsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsRow.setOpaque(false);
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        cardsRow.setPreferredSize(new Dimension(700, 150));
        cardsRow.add(statCard("THIS MONTH SPENT", spentValue, Theme.PRIMARY));
        cardsRow.add(statCard("ACTUAL SAVINGS", savingsValue, Theme.ACCENT));
        cardsRow.add(statCard("BUDGETS ON TRACK", budgetValue, Theme.WARNING));

        JPanel headerBlock = new JPanel();
        headerBlock.setOpaque(false);
        headerBlock.setLayout(new BoxLayout(headerBlock, BoxLayout.Y_AXIS));
        headerBlock.add(welcomeLabel);
        headerBlock.add(tagline);
        headerBlock.add(cardsRow);

        add(headerBlock, BorderLayout.NORTH);

        refreshData();
    }

    private RoundedPanel statCard(String label, JLabel valueLabel, Color accent) {
        RoundedPanel card = new RoundedPanel(18, Theme.CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setMaximumSize(new Dimension(40, 5));
        stripe.setPreferredSize(new Dimension(40, 5));
        stripe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(label);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(Theme.TEXT_MUTED);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));

        valueLabel.setFont(Theme.FONT_BIG_NUMBER);
        valueLabel.setForeground(Theme.TEXT_DARK);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(stripe);
        card.add(titleLabel);
        card.add(valueLabel);
        return card;
    }

    @Override
    public void refreshData() {
        try {
            double spent = expenseDAO.getTotalSpentThisMonth();
            double income = profileDAO.getMonthlyIncome();
            double savings = income - spent;
            List<Budget> budgets = budgetDAO.getAllBudgetsWithProgress();

            long onTrackCount = budgets.stream().filter(b -> b.getPercentUsed() < 80).count();

            spentValue.setText(String.format("Rs.%,.0f", spent));
            savingsValue.setText(String.format("Rs.%,.0f", savings));
            budgetValue.setText(onTrackCount + " / " + budgets.size());
        } catch (SQLException ex) {
            spentValue.setText("--");
            savingsValue.setText("--");
            budgetValue.setText("--");
        }
    }
}
