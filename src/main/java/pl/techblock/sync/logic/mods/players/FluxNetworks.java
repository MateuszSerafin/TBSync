package pl.techblock.sync.logic.mods.players;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.logic.interfaces.IPlayerSync;
import pl.techblock.sync.logic.mods.duckinterfaces.IFluxNetworksCustom;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.common.storage.FluxNetworkData;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FluxNetworks implements IPlayerSync {

    private String tableName = "FluxNetworks";

    //Used by procedure in db, i will leave it
    //important thing is there are two tables for this mod
    private String networkIDsTable = "FluxNetworksIDS";


    public FluxNetworks() {

    }


    @Override
    public void saveToDB(UUID playerUUID) {
        try {
            FluxNetworkData data = FluxNetworkData.get();
            CompoundNBT tag = ((IFluxNetworksCustom) data).writeCustom(playerUUID);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                CompressedStreamTools.writeCompressed(tag, saveTo);
            }

            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsert(playerUUID.toString(), tableName, bis);
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
    public void loadFromDB(UUID playerUUID) {
        try {
            //we need to also preload networks that are available for certain players, the tldr is if networks duplicate there is problem
            FluxNetworkData data = FluxNetworkData.get();

            List<Integer> aviableNetworksFromSQL = getAvailableNetworksForPlayer(playerUUID);

            ((IFluxNetworksCustom) data).addStaticNetworkIDS(playerUUID, aviableNetworksFromSQL);


            Blob blob = DBManager.select(playerUUID.toString(), tableName);
            if(blob == null){
                //it can happen, if it's not in db and load is called, like when first time creating an island and it tries to load nothing
                return;
            }

            CompoundNBT tag = CompressedStreamTools.readCompressed(blob.getBinaryStream());
            ((IFluxNetworksCustom) data).readCustom(tag);

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