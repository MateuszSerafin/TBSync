package pl.techblock.sync.logic.futurepack;

import futurepack.common.research.PlayerDataLoader;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.logic.mods.duckinterfaces.IFuturePackCustom;
import java.io.*;
import java.sql.Blob;
import java.util.UUID;

public class FuturePack implements IPlayerSync {

    private String tableName = "FuturePack";

    private IFuturePackCustom instance;

    public FuturePack(){
        instance = (IFuturePackCustom) PlayerDataLoader.instance;
    }

    @Override
    public void saveToDB(UUID playerUUID) {
        try {
            ByteArrayOutputStream out = instance.writeCustom(playerUUID);

            if(out == null){
                return;
            }

            byte[] compressedData = out.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsert(playerUUID.toString(), tableName, bis);
            bis.close();
        }
        catch (Exception e){
            TBSync.getLOGGER().error("Problem with FuturePack while saving data");
            e.printStackTrace();
        }

    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        try {
            Blob blob = DBManager.select(playerUUID.toString(), tableName);
            if(blob == null){
                //technically i don't do anything it looks like internally it can handle nothing
                return;
            }
            InputStream inputStream = blob.getBinaryStream();
            instance.readCustom(playerUUID, inputStream);

            inputStream.close();
            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with Future Pack while loading data");
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(UUID playerUUID) {
        instance.cleanup(playerUUID);
    }
}
