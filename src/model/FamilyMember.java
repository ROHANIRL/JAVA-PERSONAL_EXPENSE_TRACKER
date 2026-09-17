package model;

/** A household member whose body metrics drive the nutrition/essentials calculation. */
public class FamilyMember {
    private int memberId;
    private String name;
    private int age;
    private String gender; // "M" or "F"
    private double heightCm;
    private double weightKg;
    private String activityLevel; // sedentary, light, moderate, active

    public FamilyMember() {
    }

    public FamilyMember(String name, int age, String gender, double heightCm, double weightKg, String activityLevel) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.activityLevel = activityLevel;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(double heightCm) {
        this.heightCm = heightCm;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public double getBmi() {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    public String getBmiCategory() {
        double bmi = getBmi();
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    @Override
    public String toString() {
        return String.format("%s (%d, %s) - BMI %.1f (%s)", name, age, gender, getBmi(), getBmiCategory());
    }
}
