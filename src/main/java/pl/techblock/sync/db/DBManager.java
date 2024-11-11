package pl.techblock.sync.db;

import pl.techblock.sync.TBSyncConfig;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.sql.*;

public class DBManager {

    private static Connection connection;

    public static void init() throws SQLException {
        connection = DriverManager.getConnection(TBSyncConfig.JBDCString.get());
    }

    @Nullable
    public static Blob select(String userID, String table) throws SQLException {
        String query = "SELECT data FROM " + table + " WHERE userID = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBlob(1);
                } else {
                    return null;
                }
            }
        }
    }

    public static void upsert(String userID, String table, InputStream inputStream) throws SQLException {
        String query = "INSERT INTO " + table + " (userID, data) VALUES (?, ?) ON DUPLICATE KEY UPDATE data = VALUES(data)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userID);
            statement.setBlob(2, inputStream);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw e;
        }
    }
}