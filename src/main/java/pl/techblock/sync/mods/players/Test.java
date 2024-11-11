package pl.techblock.sync.mods.players;

import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.logic.interfaces.IPlayerSync;
import java.io.ByteArrayInputStream;
import java.util.Random;
import java.util.UUID;

public class Test implements IPlayerSync {

    private String tableName = "TEST";

    public Test(){

    }

    @Override
    public void saveToDB(UUID playerUUID) {
        try {
            byte[] b = new byte[20];
            new Random().nextBytes(b);
            DBManager.upsert(playerUUID.toString(), tableName, new ByteArrayInputStream(b));
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with TEST while saving data");
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        try {
            TBSync.getLOGGER().info(DBManager.select(playerUUID.toString(), tableName));
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with TEST while loading data");
            e.printStackTrace();
        }
    }
}