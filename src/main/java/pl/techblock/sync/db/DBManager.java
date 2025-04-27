package pl.techblock.sync.db;

import pl.techblock.sync.TBSync;
import pl.techblock.sync.TBSyncConfig;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

//This should be used only for cases where there is no alternative (FluxNetworksIDS etc) not to store regular information
public class DBManager {

    private static Connection connection;

    public static void init() throws SQLException {
        try {
            connection = DriverManager.getConnection((String) TBSyncConfig.config.get("jbdc"));
        } catch (Exception e){
            TBSync.getLOGGER().warn("Unable to connect to SQL, this might cause a crash depending on what mixins you need.");
        }
    }

    public static void createTable(String tableName) {
        try {
            String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (k nvarchar(255) PRIMARY KEY, v longblob())";

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.execute();
            }
        } catch (Exception e){
            TBSync.getLOGGER().error(String.format("Unable to create table %s it will definitely cause issues", tableName));
            e.printStackTrace();
        }
    }


    @Nullable
    public static Blob selectBlob(String key, String tableName) throws SQLException {
        String query = "SELECT v FROM " + tableName + " WHERE k = ?";

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
        String query = "INSERT INTO " + tableName + " (k, v) VALUES (?, ?) ON DUPLICATE KEY UPDATE v = VALUES(v)";

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
        String query = "DELETE FROM " + tableName + " WHERE k = ?";

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