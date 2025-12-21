package pl.techblock.sync.api;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public interface IWorldSync {
    public void savePerWorldModDataToDB(String worldName);
    public void loadPerWorldModDataFromDB(String worldName);
    public void worldCleanUp(String worldName);

    @Nullable
    public ByteArrayOutputStream saveWorld(String worldName);
    public void loadWorld(String worldName, @Nullable InputStream data);
}