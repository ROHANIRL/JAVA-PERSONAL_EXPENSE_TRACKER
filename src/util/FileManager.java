package util;

import model.Expense;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Handles the flat-file backup and audit log that sit alongside the MySQL database. */
public final class FileManager {

    private static final String DATA_DIR = "data";
    private static final String LOG_FILE = DATA_DIR + "/logs.txt";
    private static final String BACKUP_FILE = DATA_DIR + "/backup.txt";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FileManager() {
    }

    public static void log(String message) {
        ensureDataDirExists();
        String line = LocalDateTime.now().format(TIMESTAMP_FORMAT) + " - " + message;
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
        } catch (IOException e) {
            System.err.println("Warning: could not write to log file: " + e.getMessage());
        }
    }

    /** Built with StringBuilder since the report is assembled line-by-line in a loop. */
    public static void writeBackup(List<Expense> expenses) {
        ensureDataDirExists();

        StringBuilder report = new StringBuilder();
        report.append("Expense Backup - generated ")
                .append(LocalDateTime.now().format(TIMESTAMP_FORMAT))
                .append("\n");
        report.append("=".repeat(60)).append("\n");

        double total = 0;
        for (Expense e : expenses) {
            report.append(String.format("[%d] %-12s Rs.%-10.2f %-20s %s%n",
                    e.getExpenseId(), e.getCategoryName(), e.getAmount(), e.getDescription(), e.getDate()));
            total += e.getAmount();
        }

        report.append("=".repeat(60)).append("\n");
        report.append(String.format("Total: Rs.%.2f (%d expenses)%n", total, expenses.size()));

        try (FileWriter fw = new FileWriter(BACKUP_FILE, false);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.print(report);
        } catch (IOException e) {
            System.err.println("Warning: could not write backup file: " + e.getMessage());
        }
    }

    private static void ensureDataDirExists() {
        try {
            Files.createDirectories(Path.of(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Warning: could not create data directory: " + e.getMessage());
        }
    }
}
