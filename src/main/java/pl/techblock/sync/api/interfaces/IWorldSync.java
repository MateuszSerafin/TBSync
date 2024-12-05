package pl.techblock.sync.api.interfaces;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public interface IWorldSync {
    //this saves data that in mods are per world for example XNet saves it's networks per world
    //this should be used to save data that is not really player specific, but assigned to some world
    //internally save loading to db uses the streams
    public void savePerWorldModDataToDB(String worldName) throws Exception;
    public void loadPerWorldModDataFromDB(String worldName) throws Exception;

    @Nullable
    public ByteArrayOutputStream savePerWorldModData(String worldName) throws Exception;
    public void loadPerWorldModData(String worldName, @Nullable InputStream in) throws Exception;

    //from server perspective it should look like world was not existing before (or at least not cause issues when world is unloaded)
    public void cleanup(String worldName) throws Exception;
}
