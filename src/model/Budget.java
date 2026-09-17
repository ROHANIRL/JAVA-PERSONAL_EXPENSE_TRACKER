package model;

public class Budget {
    private int budgetId;
    private int categoryId;
    private String categoryName;
    private double monthlyLimit;
    private double spentSoFar;

    public Budget() {
    }

    public Budget(int categoryId, double monthlyLimit) {
        this.categoryId = categoryId;
        this.monthlyLimit = monthlyLimit;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(double monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public double getSpentSoFar() {
        return spentSoFar;
    }

    public void setSpentSoFar(double spentSoFar) {
        this.spentSoFar = spentSoFar;
    }

    public double getPercentUsed() {
        if (monthlyLimit == 0) return 0;
        return (spentSoFar / monthlyLimit) * 100.0;
    }
}
