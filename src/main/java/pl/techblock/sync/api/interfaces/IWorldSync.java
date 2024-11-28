package pl.techblock.sync.api.interfaces;

public interface IWorldSync {
    //this saves data that in mods are per world for example XNet saves it's networks per world
    //this should be used to save data that is not really player specific, but assigned to some world
    public void savePerWorldModData(String worldName) throws Exception;
    public void loadPerWorldModData(String worldName) throws Exception;
    //from server perspective it should look like world was not existing before (or at least not cause issues when world is unloaded)
    public void cleanup(String worldName) throws Exception;
}
