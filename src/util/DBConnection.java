package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates JDBC connections to the MySQL database.
 *
 * Credentials are deliberately not stored in source code. Configure them with
 * environment variables (EXPENSE_DB_URL, EXPENSE_DB_USER, EXPENSE_DB_PASSWORD)
 * or Java system properties (expense.db.url, expense.db.user,
 * expense.db.password). The defaults are suitable for a local MySQL install.
 */
public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/expense_tracker"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                    setting("expense.db.url", "EXPENSE_DB_URL", DEFAULT_URL),
                    setting("expense.db.user", "EXPENSE_DB_USER", "root"),
                    setting("expense.db.password", "EXPENSE_DB_PASSWORD", ""));
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Put mysql-connector-j in the lib folder.", e);
        }
    }

    private static String setting(String property, String environment, String defaultValue) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) value = System.getenv(environment);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /** Kept for compatibility with the console application. Connections are DAO-scoped. */
    public static void close() {
        // Each DAO uses try-with-resources, so there is no shared connection to close.
    }
}
