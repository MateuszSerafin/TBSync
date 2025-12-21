package pl.techblock.sync.logic.fluxnetworks;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.TBSyncConfig;
import pl.techblock.sync.api.IPlayerSync;
import pl.techblock.sync.db.DBManager;
import sonar.fluxnetworks.common.connection.FluxNetwork;
import sonar.fluxnetworks.common.connection.FluxNetworkData;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FluxNetworks implements IPlayerSync {

    private final String fluxNetworksTable = "FluxNetworks";

    public FluxNetworks() {
        DBManager.createTable(fluxNetworksTable);
        FluxIDS.init();
    }

    @Override
    public void savePlayerToDB(UUID playerUUID) {
        ByteArrayOutputStream data = savePlayer(playerUUID);
        if(data == null){
            TBSync.getLogger().info("FluxData is null for Player-UUID {}", playerUUID.toString());
            return;
        }
        DBManager.upsertBlob(playerUUID.toString(), fluxNetworksTable, new ByteArrayInputStream(data.toByteArray()));
    }

    @Override
    public void loadPlayerFromDB(UUID playerUUID) {
        Blob data = DBManager.selectBlob(playerUUID.toString(), fluxNetworksTable);
        if(data == null){
            loadPlayer(playerUUID, null);
            return;
        }
        try {
            loadPlayer(playerUUID, data.getBinaryStream());
        } catch (Exception e) {
            TBSync.getLogger().error("Issue with binary stream for FluxNetworks, Player-UUID {}", playerUUID.toString());
            TBSync.getLogger().error(e.getMessage());
        }
    }

    @Override
    public void playerCleanUp(UUID playerUUID) {
        //this is actually implemented correctly
        //we remove network and leave it in state where actual blocks still are connected to the network but it doesn't exist
        //after loading and reloading chunk it will work normally, idea is network can be loaded only on one server at a time

        List<Integer> toDelete = new ArrayList<>();

        Object instance = FluxNetworkData.getInstance();

        for (Int2ObjectMap.Entry<FluxNetwork> iFluxNetworkEntry : ((IFluxNetworksCustom) instance).getNetworks().int2ObjectEntrySet()) {
            UUID owner = iFluxNetworkEntry.getValue().getOwnerUUID();
            if(!owner.equals(playerUUID)) continue;
            toDelete.add(iFluxNetworkEntry.getIntKey());
        }

        for (Integer i : toDelete) {
            ((IFluxNetworksCustom) instance).getNetworks().remove(i);
        }

        TBSync.getLogger().info("Successfully cleaned up FluxNetworks for Player-UUID {}", playerUUID.toString());
    }

    @Nullable
    @Override
    public ByteArrayOutputStream savePlayer(UUID playerUUID) {
        try {
            Object data = FluxNetworkData.getInstance();
            CompoundTag tag = ((IFluxNetworksCustom) data).writeCustom(playerUUID);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                NbtIo.writeCompressed(tag, saveTo);
            }
            TBSync.getLogger().info("Successfully gathered FluxNetworksData for Player-UUID {}", playerUUID.toString());
            return bos;
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught gathering data for FluxNetworks, Player-UUID: {}", playerUUID);
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }

    @Override
    public void loadPlayer(UUID playerUUID, @org.jetbrains.annotations.Nullable InputStream data) {
        //we need to also preload networks that are available for certain players, the tldr is if networks duplicate there is problem
        try {
            Object fluxNetworkData = FluxNetworkData.getInstance();

            List<Integer> availableNetworksFromSQL = FluxIDS.getAvailableNetworksForPlayer(playerUUID);

            ((IFluxNetworksCustom) fluxNetworkData).addStaticNetworkIDS(playerUUID, availableNetworksFromSQL);

            if (data == null) {
                TBSync.getLogger().info("FluxData input stream is empty for Player-UUID {}", playerUUID.toString());
                return;
            }
            CompoundTag tag = NbtIo.readCompressed(data, NbtAccounter.unlimitedHeap());
            ((IFluxNetworksCustom) fluxNetworkData).readCustom(tag);
            TBSync.getLogger().info("Successfully loaded FluxNetworks for Player-UUID {} networks size {}", playerUUID.toString(), availableNetworksFromSQL.size());
            data.close();
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught loading data for FluxNetworks, Player-UUID: {}", playerUUID);
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }
}

class FluxIDS {

    private static final String fluxIDSTable = "FluxNetworksIDS";

    private static void createFluxIDSTable() {
        try {
            String query = "CREATE TABLE IF NOT EXISTS FluxNetworksIDS (userID nvarchar(255), networkID INT AUTO_INCREMENT PRIMARY KEY)";

            try (PreparedStatement ps = DBManager.getConnectionForNonStandardQuery().prepareStatement(query)) {
                ps.execute();
            }
        } catch (Exception e){
            TBSync.getLogger().error("Unable to create table {} for FluxNetworks this is a problem", fluxIDSTable);
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This is catastrophic crashing server");
        }
    }

    private static void createProcedure(){
        String query = """
                CREATE PROCEDURE IF NOT EXISTS getNetworksOrAddAndGet (IN paramUserID nvarchar(255), IN paramNetworkCount INT)
                 BEGIN
                  DECLARE currentNetworks INT;
                  DECLARE i INT DEFAULT 0;
                  SELECT COUNT(userID) INTO currentNetworks FROM FluxNetworksIDS WHERE userID=paramUserID;
                  IF currentNetworks < paramNetworkCount THEN
                    WHILE i < (paramNetworkCount - currentNetworks) DO
                      INSERT INTO FluxNetworksIDS(userID) VALUES (paramUserID);
                      SET i = i + 1;
                    END WHILE;
                  END IF;
                  SELECT networkID FROM FluxNetworksIDS WHERE userID=paramUserID;
                 END;""";
        try {
            try (PreparedStatement ps = DBManager.getConnectionForNonStandardQuery().prepareStatement(query)) {
                ps.execute();
            }
        } catch (Exception e){
            TBSync.getLogger().error("Unable to create procedure for flux networks (it won't work without it)");
            throw new RuntimeException("Fix, the issue above");
        }
    }

    public static List<Integer> getAvailableNetworksForPlayer(UUID playerUUID) {
        try {
            String query = "CALL getNetworksOrAddAndGet(?,?)";
            Connection connection = DBManager.getConnectionForNonStandardQuery();

            List<Integer> toReturn = new ArrayList<>();

            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, playerUUID.toString());
                ps.setInt(2, TBSyncConfig.privateFluxNetworksPerPlayer);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        toReturn.add(rs.getInt(1));
                    }
                }
            }
            return toReturn;
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught loading network ids for FluxNetworks, Player-UUID: {}", playerUUID);
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }

    public static void init(){
        createFluxIDSTable();
        createProcedure();
    }
}