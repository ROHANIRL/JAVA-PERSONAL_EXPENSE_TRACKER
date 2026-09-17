package service;

import dao.ExpenseDAO;
import dao.UserProfileDAO;
import model.SavingsSummary;
import util.Validation;

import java.sql.SQLException;

/** Turns raw income/expense numbers into a savings picture the user can act on. */
public class SavingsService {

    private final UserProfileDAO profileDAO = new UserProfileDAO();
    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    public SavingsSummary getSavingsSummary() throws SQLException {
        double income = profileDAO.getMonthlyIncome();
        double goalPercent = profileDAO.getSavingsGoalPercent();
        double spent = expenseDAO.getTotalSpentThisMonth();
        double savings = income - spent;
        double savingsPercent = income == 0 ? 0 : (savings / income) * 100.0;
        double goalAmount = income * (goalPercent / 100.0);
        boolean onTrack = savings >= goalAmount;
        return new SavingsSummary(income, spent, savings, savingsPercent, goalAmount, goalPercent, onTrack);
    }

    public void updateIncomeAndGoal(double income, double goalPercent) throws SQLException {
        Validation.requirePositive(income, "Monthly income");
        Validation.requireRange(goalPercent, 0, 100, "Savings goal percent");
        profileDAO.updateIncomeAndGoal(income, goalPercent);
    }
}
