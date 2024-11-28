package pl.techblock.sync.db;

import pl.techblock.sync.TBSyncConfig;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class DBManager {

    private static Connection connection;

    public static void init() throws SQLException {
        connection = DriverManager.getConnection(TBSyncConfig.JBDCString.get());
    }

    public static void createTable(String tableName) throws SQLException{
        String query = "CREATE TABLE " + tableName + " (key nvarchar(255) PRIMARY KEY, value blob(65535))";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.execute();
        }
    }


    @Nullable
    public static Blob selectBlob(String key, String tableName) throws SQLException {
        String query = "SELECT value FROM " + tableName + " WHERE key = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBlob(1);
                } else {
                    return null;
                }
            }
        }
    }

    public static void upsertBlob(String key, String tableName, InputStream inputStream) throws SQLException, IOException {
        String query = "INSERT INTO " + tableName + " (key, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, key);
            statement.setBlob(2, inputStream);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            inputStream.close();
        }
    }

    public static void deleteByKey(String key, String tableName) throws SQLException {
        String query = "DELETE FROM " + tableName + " WHERE key = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, key);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }

    public static Connection getConnectionForNonStandardQuery() {
        return connection;
    }
}