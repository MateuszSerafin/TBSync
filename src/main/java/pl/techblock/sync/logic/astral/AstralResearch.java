package pl.techblock.sync.logic.astral;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.mixins.astral.AstralResearchAccess;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.Blob;
import java.util.UUID;

public class AstralResearch implements IPlayerSync {

    private String tableName = "AstralResearch";

    public AstralResearch(){
        DBManager.createTable(tableName);
    }

    @Nullable
    @Override
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        CompoundNBT tag = new CompoundNBT();
        //getProgress always returns something no chance for null
        AstralResearchAccess.getProgress(playerUUID).store(tag);

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
            TBSync.getLOGGER().error("Problem with AstralResearch while saving data");
            e.printStackTrace();
        }
    }

    @Override
    public void loadSaveData(UUID playerUUID, InputStream in) throws Exception {
        if(in == null) return;
        CompoundNBT tag = CompressedStreamTools.readCompressed(in);
        AstralResearchAccess.load_unsafeFromNBT(playerUUID, tag);
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
            TBSync.getLOGGER().error("Problem with AstralResearch while loading data");
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(UUID playerUUID) {
        //I would need like 5 mixins to do it correctly, if that will cause issues will fix
        AstralResearchAccess.getProgress().remove(playerUUID);
    }
}