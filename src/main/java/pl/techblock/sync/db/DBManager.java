package pl.techblock.sync.db;

import pl.techblock.sync.TBSync;
import pl.techblock.sync.TBSyncConfig;
import javax.annotation.Nullable;
import java.io.InputStream;
import java.sql.*;

public class DBManager {

    private static Connection connection;

    public static void init() {
        try {
            connection = DriverManager.getConnection(TBSyncConfig.dataBaseConnection);
        } catch (Exception e) {
            TBSync.getLogger().error("Can't connect to database", e);
            throw new RuntimeException("Actually database is needed, crashing server");
        }
    }

    public static void createTable(String tableName) {
        try {
            String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (k nvarchar(255) PRIMARY KEY, v longblob)";

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.execute();
            }
        } catch (Exception e) {
            TBSync.getLogger().error("Unable to create table {}. This will definitely cause issues", tableName);
            throw new RuntimeException("Actually this is a big problem let's crash server");
        }
    }

    //Null is required. Mixins are written in a way if there is no data (such as new player) default values are populated
    @Nullable
    public static Blob selectBlob(String key, String tableName) {
        String query = "SELECT v FROM " + tableName + " WHERE k = ?";
        try {
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, key);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBlob(1);
                    }
                }
            }
        } catch (Exception e){
            TBSync.getLogger().error("Unable to select data from database, this needs to crash (otherwise userdata corruption is inevitable)");
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("Crashing... can't select data");
        }
        return null;
    }

    public static void upsertBlob(String key, String tableName, InputStream inputStream) {
        //feels like it can happen
        if(inputStream == null) {
            TBSync.getLogger().info("Tried to upsert null, Key: {}, Table: {}", key, tableName);
            return;
        }
        try {
            String query = "INSERT INTO " + tableName + " (k, v) VALUES (?, ?) ON DUPLICATE KEY UPDATE v = VALUES(v)";
            try (inputStream; PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, key);
                statement.setBlob(2, inputStream);
                statement.executeUpdate();
            }
        } catch (Exception e){
            TBSync.getLogger().error("Unable to upsert data from database, this needs to crash (otherwise userdata corruption is inevitable)");
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("Crashing... can't upsert data");
        }
    }

    //This is not yet implemented
    /*
    public static void deleteByKey(String key, String tableName) throws SQLException {
        String query = "DELETE FROM " + tableName + " WHERE k = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, key);
            statement.executeUpdate();
        }
    }
     */

    public static Connection getConnectionForNonStandardQuery() {
        return connection;
    }
}