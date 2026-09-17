-- ============================================================
-- Personal Expense Tracker - Database Schema
-- Run this first, then sample_data.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS expense_tracker;
USE expense_tracker;

CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_profile (
    profile_id INT AUTO_INCREMENT PRIMARY KEY,
    monthly_income DECIMAL(12,2) NOT NULL DEFAULT 0,
    savings_goal_percent DECIMAL(5,2) NOT NULL DEFAULT 20.00
);

-- The application always has one profile row, even when sample_data.sql is not run.
INSERT INTO user_profile (profile_id, monthly_income, savings_goal_percent)
VALUES (1, 0.00, 20.00)
ON DUPLICATE KEY UPDATE profile_id = profile_id;

CREATE TABLE IF NOT EXISTS budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    monthly_limit DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE,
    UNIQUE KEY unique_category_budget (category_id)
);

CREATE TABLE IF NOT EXISTS expenses (
    expense_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    description VARCHAR(255),
    expense_date DATE NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS family_members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    height_cm DECIMAL(5,2) NOT NULL,
    weight_kg DECIMAL(5,2) NOT NULL,
    activity_level VARCHAR(20) NOT NULL DEFAULT 'light'
);
