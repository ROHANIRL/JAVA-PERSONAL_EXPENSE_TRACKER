package model;

/** One family member's calculated essentials requirement - the output of NutritionService. */
public class MemberNutritionResult {
    private final String memberName;
    private final double bmi;
    private final String bmiCategory;
    private final double tdee;
    private final double riceKgPerMonth;
    private final double dalKgPerMonth;
    private final double oilLitersPerMonth;

    public MemberNutritionResult(String memberName, double bmi, String bmiCategory, double tdee,
                                  double riceKgPerMonth, double dalKgPerMonth, double oilLitersPerMonth) {
        this.memberName = memberName;
        this.bmi = bmi;
        this.bmiCategory = bmiCategory;
        this.tdee = tdee;
        this.riceKgPerMonth = riceKgPerMonth;
        this.dalKgPerMonth = dalKgPerMonth;
        this.oilLitersPerMonth = oilLitersPerMonth;
    }

    public String getMemberName() {
        return memberName;
    }

    public double getBmi() {
        return bmi;
    }

    public String getBmiCategory() {
        return bmiCategory;
    }

    public double getTdee() {
        return tdee;
    }

    public double getRiceKgPerMonth() {
        return riceKgPerMonth;
    }

    public double getDalKgPerMonth() {
        return dalKgPerMonth;
    }

    public double getOilLitersPerMonth() {
        return oilLitersPerMonth;
    }
}
