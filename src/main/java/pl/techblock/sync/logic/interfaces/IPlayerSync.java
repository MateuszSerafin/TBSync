package pl.techblock.sync.logic.interfaces;

import java.util.UUID;

public interface IPlayerSync {
    public void saveToDB(UUID playerUUID);
    public void loadFromDB(UUID playerUUID);
    //from server perspective it should look like player never joined (ideally)
    public void cleanup(UUID playerUUID);
}