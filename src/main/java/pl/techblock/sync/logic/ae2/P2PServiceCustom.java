package pl.techblock.sync.logic.ae2;

import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

//the problem is similar to FluxNetworks
//the P2P IDs are randomly generated sometimes there is collision and player's p2p tunnel disconnects
//SQL keeps track of used IDs
//There is a better implementation however I don't have effort to write it
//Anyway the frequency is generated once per block. And then it stays in NBT data so unless someone will heavily abuse it
//It will just cause small lag spikes
public class P2PServiceCustom {

    private static final String databaseTable = "AE2P2PIDs";

    public static void createP2PTable() {
        try {
            String query = "CREATE TABLE IF NOT EXISTS " + databaseTable + " (p2pID INT AUTO_INCREMENT PRIMARY KEY)";

            try (PreparedStatement ps = DBManager.getConnectionForNonStandardQuery().prepareStatement(query)) {
                ps.execute();
            }
        } catch (Exception e){
            TBSync.getLogger().error("Unable to create table {} for P2PIDs this is a problem", databaseTable);
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This is catastrophic crashing server");
        }
    }

    public static Integer getp2Pidincrementallybetweensservers() {
        try {
            String query = "INSERT INTO " + databaseTable + " values() RETURNING p2pID";
            Connection connection = DBManager.getConnectionForNonStandardQuery();

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            throw new RuntimeException("This shouldn't happen, let's crash");
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught loading network ids for P2P, Something with database 100%");
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }
}