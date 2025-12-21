package pl.techblock.sync.api;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

public interface IPlayerSync {
    public void savePlayerToDB(UUID playerUUID);
    public void loadPlayerFromDB(UUID playerUUID);
    public void playerCleanUp(UUID playerUUID);

    @Nullable
    public ByteArrayOutputStream savePlayer(UUID playerUUID);
    public void loadPlayer(UUID playerUUID, @Nullable InputStream data);
}