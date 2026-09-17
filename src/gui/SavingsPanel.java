package gui;

import dao.ExpenseDAO;
import dao.UserProfileDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class SavingsPanel extends JPanel implements Refreshable {

    private final UserProfileDAO profileDAO = new UserProfileDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    private final JTextField incomeField = new JTextField(12);
    private final JTextField goalField = new JTextField(6);
    private final JLabel resultArea = new JLabel();
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    public SavingsPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(30, 34, 30, 34));

        JLabel pageTitle = new JLabel("Savings Tracker");
        pageTitle.setFont(Theme.FONT_TITLE);
        pageTitle.setForeground(Theme.TEXT_DARK);
        pageTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        incomeField.setFont(Theme.FONT_BODY);
        goalField.setFont(Theme.FONT_BODY);

        RoundedButton saveBtn = new RoundedButton("Update", Theme.PRIMARY, Theme.PRIMARY_DARK);
        saveBtn.addActionListener(e -> saveProfile());

        JLabel incomeLabel = new JLabel("Monthly Income (Rs.):");
        incomeLabel.setFont(Theme.FONT_BODY_BOLD);
        JLabel goalLabel = new JLabel("Savings Goal (%):");
        goalLabel.setFont(Theme.FONT_BODY_BOLD);

        RoundedPanel formCard = new RoundedPanel(18, Theme.CARD_BG);
        formCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 10));
        formCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        formCard.add(incomeLabel);
        formCard.add(incomeField);
        formCard.add(goalLabel);
        formCard.add(goalField);
        formCard.add(saveBtn);

        resultArea.setFont(Theme.FONT_BODY);
        resultArea.setVerticalAlignment(SwingConstants.TOP);

        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(340, 26));
        progressBar.setFont(Theme.FONT_BODY_BOLD);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel progressLabel = new JLabel("Progress toward savings goal");
        progressLabel.setFont(Theme.FONT_BODY_BOLD);
        progressLabel.setForeground(Theme.TEXT_MUTED);
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedPanel resultCard = new RoundedPanel(18, Theme.CARD_BG);
        resultCard.setLayout(new BoxLayout(resultCard, BoxLayout.Y_AXIS));
        resultCard.setBorder(BorderFactory.createEmptyBorder(26, 26, 26, 26));
        resultArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultCard.add(resultArea);
        resultCard.add(Box.createVerticalStrut(20));
        resultCard.add(progressLabel);
        resultCard.add(Box.createVerticalStrut(8));
        resultCard.add(progressBar);

        JPanel headerBlock = new JPanel();
        headerBlock.setOpaque(false);
        headerBlock.setLayout(new BoxLayout(headerBlock, BoxLayout.Y_AXIS));
        formCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerBlock.add(pageTitle);
        headerBlock.add(formCard);
        headerBlock.add(Box.createVerticalStrut(20));
        headerBlock.add(resultCard);

        add(headerBlock, BorderLayout.NORTH);
    }

    private void saveProfile() {
        double income, goal;
        try {
            income = Double.parseDouble(incomeField.getText().trim());
            goal = Double.parseDouble(goalField.getText().trim());
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Enter valid numbers for income and goal.",
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            profileDAO.updateIncomeAndGoal(income, goal);
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void refreshData() {
        try {
            double income = profileDAO.getMonthlyIncome();
            double goalPercent = profileDAO.getSavingsGoalPercent();
            double spent = expenseDAO.getTotalSpentThisMonth();
            double actualSavings = income - spent;
            double actualSavingsPercent = income == 0 ? 0 : (actualSavings / income) * 100.0;
            double goalAmount = income * (goalPercent / 100.0);

            incomeField.setText(String.valueOf(income));
            goalField.setText(String.valueOf(goalPercent));

            boolean onTrack = actualSavings >= goalAmount;
            String statusLine = onTrack
                    ? "On track! You're meeting your savings goal."
                    : String.format("Behind goal by Rs.%.2f. Consider trimming discretionary categories.",
                        goalAmount - actualSavings);

            resultArea.setText(String.format(
                    "<html>Monthly Income: <b>Rs.%.2f</b><br>" +
                    "Total Spent This Month: <b>Rs.%.2f</b><br>" +
                    "Actual Savings: <b>Rs.%.2f</b> (%.1f%% of income)<br>" +
                    "Savings Goal: <b>Rs.%.2f</b> (%.1f%% of income)<br><br>" +
                    "<span style='color:%s'>%s</span></html>",
                    income, spent, actualSavings, actualSavingsPercent, goalAmount, goalPercent,
                    onTrack ? "#2E7D32" : "#C62828", statusLine));

            int progressPercent = goalAmount <= 0 ? 0 :
                    (int) Math.max(0, Math.min(100, (actualSavings / goalAmount) * 100));
            progressBar.setValue(progressPercent);
            progressBar.setString(progressPercent + "% of goal reached");
            progressBar.setForeground(onTrack ? Theme.PRIMARY : Theme.WARNING);

        } catch (SQLException ex) {
            resultArea.setText("Error loading savings summary: " + ex.getMessage());
        }
    }
}
