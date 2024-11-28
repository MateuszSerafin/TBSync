package pl.techblock.sync.api.interfaces;

import java.util.UUID;

public interface IPlayerSync {
    public void saveToDB(UUID playerUUID) throws Exception;
    public void loadFromDB(UUID playerUUID) throws Exception;
    //from server perspective it should look like player never joined (ideally)
    public void cleanup(UUID playerUUID) throws Exception;
}