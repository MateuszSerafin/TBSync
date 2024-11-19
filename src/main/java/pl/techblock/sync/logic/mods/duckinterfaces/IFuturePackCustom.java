package pl.techblock.sync.logic.mods.duckinterfaces;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

public interface IFuturePackCustom {
    //don't use nbt as i can write just input output streams to database and author doesn't use nbt anywhere
    public void readCustom(UUID playerUUID, InputStream inputStream);
    public ByteArrayOutputStream writeCustom(UUID pUUID);
    public void cleanup(UUID playerUUID);
}
