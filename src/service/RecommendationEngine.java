package service;

import model.Budget;
import model.EssentialsPlan;
import model.MemberNutritionResult;
import model.SavingsSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule-based "smart advisor" - the project's AI-powered layer.
 *
 * Each rule inspects the household's current numbers (essentials cost, savings
 * progress, budget status, per-member BMI) and produces a plain-language
 * recommendation. This is deliberately a transparent, explainable rule/scoring
 * engine (expert systems are a classical branch of AI) rather than a black-box
 * model - it stays fully offline and every recommendation can be traced back to
 * the exact rule that produced it, which is easier to defend in a viva than an
 * opaque API call.
 */
public class RecommendationEngine {

    private static final double WARNING_THRESHOLD_PERCENT = 80.0;

    public List<String> analyze(EssentialsPlan plan, SavingsSummary savings, List<Budget> budgets) {
        List<String> recommendations = new ArrayList<>();

        // Rule 1: essential groceries eating too much of total income
        if (plan.getEstimatedMonthlyCost() > 0 && savings.getIncome() > 0
                && plan.getEstimatedMonthlyCost() > savings.getIncome() * 0.4) {
            recommendations.add(String.format(
                    "Essential groceries (Rs.%.0f) are over 40%% of your income. Consider reviewing "
                            + "discretionary categories like Entertainment or Shopping to free up room.",
                    plan.getEstimatedMonthlyCost()));
        }

        // Rule 2: per-member BMI-based notes
        for (MemberNutritionResult r : plan.getMemberResults()) {
            if ("Underweight".equals(r.getBmiCategory())) {
                recommendations.add(r.getMemberName() + " is in the underweight range - their plan includes "
                        + "a higher staple allocation to support healthy weight gain. Consider a doctor's "
                        + "input for a full dietary plan.");
            } else if ("Obese".equals(r.getBmiCategory())) {
                recommendations.add(r.getMemberName() + "'s plan includes a reduced staple allocation. "
                        + "This is a general estimate only - consult a healthcare professional for personalized advice.");
            }
        }

        // Rule 3: savings goal status
        if (!savings.isOnTrack()) {
            recommendations.add("You're behind your savings goal this month. " + savings.getStatusMessage());
        }

        // Rule 4: any budget category over its limit
        for (Budget b : budgets) {
            if (b.getPercentUsed() >= 100) {
                recommendations.add(b.getCategoryName() + " is over its budget this month - "
                        + "that's the first place to cut back.");
            } else if (b.getPercentUsed() >= WARNING_THRESHOLD_PERCENT) {
                recommendations.add(b.getCategoryName() + " is close to its budget limit this month.");
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Everything looks on track - essentials fit your budget and "
                    + "you're meeting your savings goal.");
        }

        return recommendations;
    }
}
