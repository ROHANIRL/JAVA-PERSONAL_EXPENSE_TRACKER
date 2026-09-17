package model;

import java.util.List;

/** Household-level essentials plan: every member's breakdown plus totals and estimated cost. */
public class EssentialsPlan {
    private final List<MemberNutritionResult> memberResults;
    private final double totalRiceKg;
    private final double totalDalKg;
    private final double totalOilLiters;
    private final double estimatedMonthlyCost;

    public EssentialsPlan(List<MemberNutritionResult> memberResults, double totalRiceKg, double totalDalKg,
                           double totalOilLiters, double estimatedMonthlyCost) {
        this.memberResults = memberResults;
        this.totalRiceKg = totalRiceKg;
        this.totalDalKg = totalDalKg;
        this.totalOilLiters = totalOilLiters;
        this.estimatedMonthlyCost = estimatedMonthlyCost;
    }

    public List<MemberNutritionResult> getMemberResults() {
        return memberResults;
    }

    public double getTotalRiceKg() {
        return totalRiceKg;
    }

    public double getTotalDalKg() {
        return totalDalKg;
    }

    public double getTotalOilLiters() {
        return totalOilLiters;
    }

    public double getEstimatedMonthlyCost() {
        return estimatedMonthlyCost;
    }
}
