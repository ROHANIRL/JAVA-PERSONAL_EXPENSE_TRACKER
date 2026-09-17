package menu;

import java.util.Scanner;

/** Displays the console menu and reads raw input. Pure I/O - no business logic. */
public class Menu {

    private final Scanner scanner;

    public Menu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void printBanner() {
        System.out.println("========================================");
        System.out.println("   PERSONAL EXPENSE TRACKER");
        System.out.println("   Spend smart. Save more.");
        System.out.println("========================================");
    }

    public void printMenu() {
        System.out.println("\n---------------- MENU ----------------");
        System.out.println("1. Add Expense");
        System.out.println("2. View All Expenses");
        System.out.println("3. View This Month's Expenses");
        System.out.println("4. Delete Expense");
        System.out.println("5. Set Category Budget");
        System.out.println("6. View Budget Status");
        System.out.println("7. Set Monthly Income & Savings Goal");
        System.out.println("8. View Savings Summary");
        System.out.println("9. Add New Category");
        System.out.println("10. Backup Expenses to File");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    public String readChoice() {
        return scanner.nextLine().trim();
    }

    public String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }
}
