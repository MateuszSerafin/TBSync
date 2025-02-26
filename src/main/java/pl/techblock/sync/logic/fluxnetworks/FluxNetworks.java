package pl.techblock.sync.logic.fluxnetworks;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.common.storage.FluxNetworkData;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FluxNetworks implements IPlayerSync {

    private String tableName = "FluxNetworks";

    //Used by procedure in db, i will leave it
    //important thing is there are two tables for this mod
    private String networkIDsTable = "FluxNetworksIDS";

    private void createSecondTable(){
        try {
            String query = "CREATE TABLE IF NOT EXISTS FluxNetworksIDS (userID nvarchar(255), networkID INT AUTO_INCREMENT PRIMARY KEY)";

            try (PreparedStatement ps = DBManager.getConnectionForNonStandardQuery().prepareStatement(query)) {
                ps.execute();
            }
        } catch (Exception e){
            TBSync.getLOGGER().error(String.format("Unable to create table %s it will definitely cause issues", tableName));
            e.printStackTrace();
        }
    }

    private void createProcedure(){
        String query = """
                CREATE PROCEDURE IF NOT EXISTS getNetworksOrAddAndGet (IN paramUserID nvarchar(255))
                 BEGIN
                  DECLARE currentNetworks INT;
                  SELECT COUNT(userID) INTO currentNetworks FROM FluxNetworksIDS WHERE userID=paramUserID;
                  IF currentNetworks < 3 THEN
                    INSERT INTO FluxNetworksIDS(userID) VALUES (paramUserID);
                    INSERT INTO FluxNetworksIDS(userID) VALUES (paramUserID);
                    INSERT INTO FluxNetworksIDS(userID) VALUES (paramUserID);
                  END IF;
                  SELECT networkID FROM FluxNetworksIDS WHERE userID=paramUserID;
                 END;""";

        try {
            try (PreparedStatement ps = DBManager.getConnectionForNonStandardQuery().prepareStatement(query)) {
                ps.execute();
            }
        } catch (Exception e){
            TBSync.getLOGGER().error("Unable to create procedure for flux networks (it won't work without it)");
            e.printStackTrace();
        }
    }

    public FluxNetworks() {
        DBManager.createTable(tableName);
        createSecondTable();
        createProcedure();
    }

    @Nullable
    @Override
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        FluxNetworkData data = FluxNetworkData.get();
        CompoundNBT tag = ((IFluxNetworksCustom) data).writeCustom(playerUUID);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream saveTo = new BufferedOutputStream(bos)) {
            CompressedStreamTools.writeCompressed(tag, saveTo);
        }
        return bos;
    }

    @Override
    public void saveToDB(UUID playerUUID) {
        try {
            ByteArrayOutputStream bos = getSaveData(playerUUID);
            if(bos == null) return;
            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsertBlob(playerUUID.toString(), tableName, bis);
            bis.close();
        }
        catch (Exception e){
            TBSync.getLOGGER().error("Problem with FluxNetworks while saving data");
            e.printStackTrace();
        }
    }

    private List<Integer> getAvailableNetworksForPlayer(UUID pUUID)  throws SQLException {
        String query = "CALL getNetworksOrAddAndGet(?)";
        Connection connection = DBManager.getConnectionForNonStandardQuery();


        List<Integer> toReturn = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, pUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    toReturn.add(rs.getInt(1));
                }
            }
        }
        return toReturn;
    }

    @Override
    public void loadSaveData(UUID playerUUID, InputStream in) throws Exception {
        //we need to also preload networks that are available for certain players, the tldr is if networks duplicate there is problem
        FluxNetworkData data = FluxNetworkData.get();

        List<Integer> aviableNetworksFromSQL = getAvailableNetworksForPlayer(playerUUID);

        ((IFluxNetworksCustom) data).addStaticNetworkIDS(playerUUID, aviableNetworksFromSQL);

        if(in == null) return;
        CompoundNBT tag = CompressedStreamTools.readCompressed(in);
        ((IFluxNetworksCustom) data).readCustom(tag);
        in.close();
    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        try {
            Blob blob = DBManager.selectBlob(playerUUID.toString(), tableName);
            if(blob == null){
                //it can happen, if it's not in db and load is called, like when first time creating an island and it tries to load nothing
                return;
            }
            loadSaveData(playerUUID, blob.getBinaryStream());
            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with FluxNetworks while loading data");
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(UUID playerUUID) {
        //this is actually implemented correctly
        //we remove network and leave it in state where actual blocks still are connected to the network but it doesn't exist
        //after loading and reloading chunk it will work normally, idea is network can be loaded only on one server at a time

        List<Integer> toDelete = new ArrayList<>();

        IFluxNetworksCustom instance = (IFluxNetworksCustom) FluxNetworkData.get();

        for (Int2ObjectMap.Entry<IFluxNetwork> iFluxNetworkEntry : instance.getNetworks().int2ObjectEntrySet()) {
            UUID owner = iFluxNetworkEntry.getValue().getOwnerUUID();
            if(!owner.equals(playerUUID)) continue;
            toDelete.add(iFluxNetworkEntry.getIntKey());
        }

        for (Integer i : toDelete) {
            instance.getNetworks().remove(i);
        }
    }
}