package service;

import dao.ExpenseDAO;
import model.Expense;
import util.FileManager;
import util.Validation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Business logic for expenses: validates input, logs every change, can produce a text backup. */
public class ExpenseService {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();

    public void addExpense(int categoryId, double amount, String description, LocalDate date) throws SQLException {
        Validation.requirePositive(amount, "Amount");
        Expense expense = new Expense(categoryId, amount, description, date);
        expenseDAO.addExpense(expense);
        FileManager.log("Added " + expenseDAO.getEntityName() + ": Rs." + amount
                + " (" + description + ") on " + date);
    }

    /** Overloaded version - defaults to today's date. Demonstrates method overloading. */
    public void addExpense(int categoryId, double amount, String description) throws SQLException {
        addExpense(categoryId, amount, description, LocalDate.now());
    }

    public List<Expense> getAllExpenses() throws SQLException {
        return expenseDAO.getAllExpenses();
    }

    public List<Expense> getExpensesForCurrentMonth() throws SQLException {
        return expenseDAO.getExpensesForCurrentMonth();
    }

    public double getTotalSpentThisMonth() throws SQLException {
        return expenseDAO.getTotalSpentThisMonth();
    }

    public boolean deleteExpense(int expenseId) throws SQLException {
        boolean deleted = expenseDAO.deleteExpense(expenseId);
        if (deleted) {
            FileManager.log("Deleted expense #" + expenseId);
        }
        return deleted;
    }

    public void backupAllExpenses() throws SQLException {
        FileManager.writeBackup(expenseDAO.getAllExpenses());
    }
}
