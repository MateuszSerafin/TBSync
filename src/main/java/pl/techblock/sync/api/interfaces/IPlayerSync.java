package pl.techblock.sync.api.interfaces;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

public interface IPlayerSync {
    //internally save loading to db uses the streams
    public void saveToDB(UUID playerUUID) throws Exception;
    public void loadFromDB(UUID playerUUID) throws Exception;

    @Nullable
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception;
    public void loadSaveData(UUID playerUUID, @Nullable InputStream in) throws Exception;

    //from server perspective it should look like player never joined (ideally)
    public void cleanup(UUID playerUUID) throws Exception;
}