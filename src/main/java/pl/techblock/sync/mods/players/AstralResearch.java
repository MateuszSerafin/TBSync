package pl.techblock.sync.mods.players;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.logic.interfaces.IPlayerSync;
import pl.techblock.sync.mixins.AstralResearchInvoker;
import java.io.*;
import java.sql.Blob;
import java.util.UUID;

public class AstralResearch implements IPlayerSync {

    private String tableName = "AstralResearch";

    public AstralResearch(){

    }

    @Override
    public void saveToDB(UUID playerUUID) {
        try {
            CompoundNBT tag = new CompoundNBT();
            AstralResearchInvoker.getProgress(playerUUID).store(tag);


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
            TBSync.getLOGGER().error("Problem with AstralResearch while saving data");
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        try {
            Blob blob = DBManager.select(playerUUID.toString(), tableName);
            if(blob == null){
                //it can happen, if it's not in db and load is called, like when first time creating an island and it tries to load nothing
                return;
            }

            CompoundNBT tag = CompressedStreamTools.readCompressed(blob.getBinaryStream());
            AstralResearchInvoker.load_unsafeFromNBT(playerUUID, tag);

            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with AstralResearch while loading data");
            e.printStackTrace();
        }
    }
}