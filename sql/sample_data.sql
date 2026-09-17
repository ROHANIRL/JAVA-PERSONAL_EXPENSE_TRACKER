-- ============================================================
-- Personal Expense Tracker - Sample / Seed Data
-- Run schema.sql first, then this file.
-- ============================================================

USE expense_tracker;

INSERT IGNORE INTO categories (category_name) VALUES
    ('Food'), ('Rent'), ('Transport'), ('Utilities'),
    ('Entertainment'), ('Healthcare'), ('Shopping'), ('Other');

INSERT INTO user_profile (monthly_income, savings_goal_percent)
SELECT 30000.00, 20.00
WHERE NOT EXISTS (SELECT 1 FROM user_profile);
