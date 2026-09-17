package service;

import dao.FamilyMemberDAO;
import model.EssentialsPlan;
import model.FamilyMember;
import model.MemberNutritionResult;
import util.Validation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts each family member's body metrics into a household essentials (rice/dal/oil) plan.
 *
 * Method: Mifflin-St Jeor BMR -> scaled by activity level to get TDEE -> macro-nutrient
 * split typical of an Indian diet (~55% carbs, ~15% protein, ~27% fat) -> converted to
 * grams of rice/dal/oil using standard caloric densities, with a small BMI-based adjustment
 * on the carbohydrate (staple) portion. This is a simplified educational model for a college
 * project, not medical or dietary advice - a disclaimer is also shown in the UI.
 */
public class NutritionService {

    private final FamilyMemberDAO familyMemberDAO = new FamilyMemberDAO();

    // Approximate market prices (Rs.) - adjust to local prices as needed
    private static final double RICE_PRICE_PER_KG = 45.0;
    private static final double DAL_PRICE_PER_KG = 110.0;
    private static final double OIL_PRICE_PER_LITER = 160.0;

    // Caloric density references
    private static final double RICE_KCAL_PER_G = 3.45;      // raw white rice, ~345 kcal/100g
    private static final double DAL_PROTEIN_FRACTION = 0.23;  // ~23% protein by weight, raw
    private static final double OIL_KCAL_PER_G = 9.0;         // pure fat
    private static final double PROTEIN_KCAL_PER_G = 4.0;

    public void addFamilyMember(String name, int age, String gender, double heightCm,
                                 double weightKg, String activityLevel) throws SQLException {
        Validation.requireNotBlank(name, "Name");
        Validation.requirePositive(heightCm, "Height");
        Validation.requirePositive(weightKg, "Weight");
        FamilyMember member = new FamilyMember(name, age, gender, heightCm, weightKg, activityLevel);
        familyMemberDAO.addMember(member);
    }

    public List<FamilyMember> getAllMembers() throws SQLException {
        return familyMemberDAO.getAllMembers();
    }

    public boolean deleteMember(int memberId) throws SQLException {
        return familyMemberDAO.deleteMember(memberId);
    }

    private double activityMultiplier(String activityLevel) {
        if (activityLevel == null) return 1.375;
        return switch (activityLevel.toLowerCase()) {
            case "sedentary" -> 1.2;
            case "moderate" -> 1.55;
            case "active" -> 1.725;
            default -> 1.375; // "light" and anything unrecognized
        };
    }

    private double bmiAdjustmentFactor(String bmiCategory) {
        return switch (bmiCategory) {
            case "Underweight" -> 1.10;
            case "Overweight" -> 0.90;
            case "Obese" -> 0.85;
            default -> 1.0; // Normal
        };
    }

    public MemberNutritionResult calculateForMember(FamilyMember member) {
        double bmi = member.getBmi();
        String bmiCategory = member.getBmiCategory();

        double bmr;
        if ("F".equalsIgnoreCase(member.getGender())) {
            bmr = 10 * member.getWeightKg() + 6.25 * member.getHeightCm() - 5 * member.getAge() - 161;
        } else {
            bmr = 10 * member.getWeightKg() + 6.25 * member.getHeightCm() - 5 * member.getAge() + 5;
        }

        double tdee = bmr * activityMultiplier(member.getActivityLevel());
        double adjustment = bmiAdjustmentFactor(bmiCategory);

        double carbCalories = tdee * 0.55 * adjustment;
        double proteinCalories = tdee * 0.15;
        double fatCalories = tdee * 0.27;

        double dailyRiceGrams = carbCalories / RICE_KCAL_PER_G;
        double dailyProteinGrams = proteinCalories / PROTEIN_KCAL_PER_G;
        double dailyDalGrams = dailyProteinGrams / DAL_PROTEIN_FRACTION;
        double dailyOilGrams = fatCalories / OIL_KCAL_PER_G;

        double riceKgPerMonth = (dailyRiceGrams * 30) / 1000.0;
        double dalKgPerMonth = (dailyDalGrams * 30) / 1000.0;
        double oilLitersPerMonth = (dailyOilGrams * 30) / 1000.0; // approximating oil density as 1g = 1ml

        return new MemberNutritionResult(member.getName(), bmi, bmiCategory, tdee,
                riceKgPerMonth, dalKgPerMonth, oilLitersPerMonth);
    }

    public EssentialsPlan generateHouseholdPlan() throws SQLException {
        List<FamilyMember> members = familyMemberDAO.getAllMembers();
        List<MemberNutritionResult> results = new ArrayList<>();

        double totalRice = 0, totalDal = 0, totalOil = 0;
        for (FamilyMember member : members) {
            MemberNutritionResult result = calculateForMember(member);
            results.add(result);
            totalRice += result.getRiceKgPerMonth();
            totalDal += result.getDalKgPerMonth();
            totalOil += result.getOilLitersPerMonth();
        }

        double estimatedCost = totalRice * RICE_PRICE_PER_KG
                + totalDal * DAL_PRICE_PER_KG
                + totalOil * OIL_PRICE_PER_LITER;

        return new EssentialsPlan(results, totalRice, totalDal, totalOil, estimatedCost);
    }
}
