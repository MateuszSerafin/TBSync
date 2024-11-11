package pl.techblock.sync.logic.interfaces;

import java.util.UUID;

public interface IPlayerSync {
    public void saveToDB(UUID playerUUID);
    public void loadFromDB(UUID playerUUID);
}
