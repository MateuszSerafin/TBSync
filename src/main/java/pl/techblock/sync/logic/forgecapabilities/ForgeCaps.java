package pl.techblock.sync.logic.forgecapabilities;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.db.DBManager;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.UUID;

//we exclude curios because we synchronize it differently
//also this cannot load player data in if player is already online (i would need mixin for every mod to inject data)
//todo pos is not saved and players are yeeted to the void, probably get overworld and check for safe block to spawn
public class ForgeCaps implements IPlayerSync {

    private ArrayList<String> exclusions = new ArrayList<>();

    private String tableName = "ForgeCaps";

    public ForgeCaps(){
        DBManager.createTable(tableName);
        exclusions.add("curios:inventory");
    }

    @Nullable
    @Override
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        ServerPlayerEntity onlinePLayer = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID);

        if(onlinePLayer != null){
            CompoundNBT saveHere = new CompoundNBT();
            onlinePLayer.getEntity().saveWithoutId(saveHere);
            CompoundNBT caps = saveHere.getCompound("ForgeCaps");
            for (String exclusion : exclusions) {
                caps.remove(exclusion);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(caps, out);
            return out;
        }

        File file = new File("./world/playerdata/" + playerUUID.toString() + ".dat");
        if(!file.exists()) return null;
        CompoundNBT nbt = CompressedStreamTools.readCompressed(file);

        CompoundNBT caps = nbt.getCompound("ForgeCaps");
        for (String exclusion : exclusions) {
            caps.remove(exclusion);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(caps, out);
        return out;
    }

    //on arclight-forge-1.16.5-1.0.25.jar after player logs out the player.dat tag is saved instantly
    //its pretty sensible to use it
    @Override
    public void saveToDB(UUID playerUUID) throws Exception {
        try {
            ByteArrayOutputStream bos = getSaveData(playerUUID);
            if(bos == null) return;
            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsertBlob(playerUUID.toString(), tableName, bis);
            bis.close();
        }
        catch (Exception e){
            TBSync.getLOGGER().error("Problem with ForgeCaps while saving data");
            e.printStackTrace();
        }
    }

    //yea no way to load it this way, only from player.dat
    //if player logs in before this is called there is a problem (i tried other ways)
    //i also don't really want to make mixin for every capability
    //this is crappy but will work assuming my code is correctly called by island plugin
    @Override
    public void loadSaveData(UUID playerUUID, InputStream in) throws Exception {
        if(in == null) return;
        CompoundNBT forgeCapsFromDB = CompressedStreamTools.readCompressed(in);

        File file = new File("./world/playerdata/" + playerUUID.toString() + ".dat");

        if(!file.exists()){
            CompoundNBT toSave = new CompoundNBT();
            toSave.put("ForgeCaps", forgeCapsFromDB);
            CompressedStreamTools.writeCompressed(toSave, file);
            return;
        }
        CompoundNBT local = CompressedStreamTools.readCompressed(file);
        local.put("ForgeCaps", forgeCapsFromDB);
        CompressedStreamTools.writeCompressed(local, file);
        in.close();
    }

    @Override
    public void loadFromDB(UUID playerUUID) throws Exception {
        try {
            Blob blob = DBManager.selectBlob(playerUUID.toString(), tableName);
            if(blob == null){
                return;
            }
            loadSaveData(playerUUID, blob.getBinaryStream());
            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with Forge caps while loading data");
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(UUID playerUUID) throws Exception {
        //don't remove existing player.dat, everything is saved to player.dat automatically when player joins
        //nothing to do from here
    }
}
