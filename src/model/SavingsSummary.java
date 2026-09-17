package model;

public class SavingsSummary {
    private final double income;
    private final double spent;
    private final double savings;
    private final double savingsPercent;
    private final double goalAmount;
    private final double goalPercent;
    private final boolean onTrack;

    public SavingsSummary(double income, double spent, double savings, double savingsPercent,
                           double goalAmount, double goalPercent, boolean onTrack) {
        this.income = income;
        this.spent = spent;
        this.savings = savings;
        this.savingsPercent = savingsPercent;
        this.goalAmount = goalAmount;
        this.goalPercent = goalPercent;
        this.onTrack = onTrack;
    }

    public double getIncome() {
        return income;
    }

    public double getSpent() {
        return spent;
    }

    public double getSavings() {
        return savings;
    }

    public double getSavingsPercent() {
        return savingsPercent;
    }

    public double getGoalAmount() {
        return goalAmount;
    }

    public double getGoalPercent() {
        return goalPercent;
    }

    public boolean isOnTrack() {
        return onTrack;
    }

    public String getStatusMessage() {
        if (onTrack) {
            return "On track! You're meeting your savings goal.";
        }
        double shortfall = goalAmount - savings;
        return String.format("Behind goal by Rs.%.2f. Consider trimming discretionary categories.", shortfall);
    }
}
