package pl.techblock.sync.logic.interfaces;

import java.util.UUID;

public interface IWorldSync {
    //tldr it doesn't save world regions but some mods save to weird locations and per world rather than per player
    //name is misleading not sure how to name it more proprely
    public void saveWorldDataToDB(UUID worldUUID);
    public void loadWorldDataFromDB(UUID worldUUID);
    //from server perspective it should look like world was not existing before, idk might not be used
    public void cleanup(UUID playerUUID);
}
