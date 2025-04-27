package pl.techblock.sync.logic.cosmeticarmor;

import lain.mods.cos.impl.ModObjects;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import javax.annotation.Nullable;
import java.io.*;
import java.util.UUID;

public class CosmeticArmor {

    private ICosmeticArmorCustom instance;

    public CosmeticArmor(){
        instance = (ICosmeticArmorCustom) ModObjects.invMan;
    }

    @Nullable
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        CompoundNBT tag = instance.writeCustom(playerUUID);

        if(tag == null){
            TBSync.getLOGGER().error("This is edge case theoretically it should be there, something with not initializing cache for cosmetic armor");
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream saveTo = new BufferedOutputStream(bos)) {
            CompressedStreamTools.writeCompressed(tag, saveTo);
        }
        return bos;
    }

    public void loadSaveData(UUID playerUUID, InputStream in) throws Exception {
        if(in == null){
            instance.readCustom(playerUUID, null);
            return;
        }
        instance.readCustom(playerUUID, CompressedStreamTools.readCompressed(in));
        in.close();
    }

    public void cleanup(UUID playerUUID) {
        instance.invalidate(playerUUID);
    }
}
