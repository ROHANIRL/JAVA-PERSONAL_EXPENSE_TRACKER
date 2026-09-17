package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Shared base for all DAOs. Demonstrates inheritance (every concrete DAO extends this)
 * and abstraction (getEntityName is abstract - each DAO must declare what it manages).
 */
public abstract class BaseDAO {

    public abstract String getEntityName();

    protected Connection getConnection() throws SQLException {
        return DBConnection.getConnection();
    }
}
