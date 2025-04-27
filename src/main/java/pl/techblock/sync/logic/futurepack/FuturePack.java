package pl.techblock.sync.logic.futurepack;

import futurepack.common.research.PlayerDataLoader;
import javax.annotation.Nullable;
import java.io.*;
import java.util.UUID;

public class FuturePack {

    private IFuturePackCustom instance;

    public FuturePack(){
        instance = (IFuturePackCustom) PlayerDataLoader.instance;
    }

    @Nullable
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        return instance.writeCustom(playerUUID);
    }

    public void loadSaveData(UUID playerUUID, InputStream in) throws Exception {
        if(in == null) return;
        instance.readCustom(playerUUID, in);
    }

    public void cleanup(UUID playerUUID) {
        instance.cleanup(playerUUID);
    }
}
