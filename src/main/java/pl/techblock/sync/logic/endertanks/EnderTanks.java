package pl.techblock.sync.logic.endertanks;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.mixins.endertanks.EnderTanksAccess;
import shetiphian.endertanks.common.misc.EnderContainer;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Map;
import java.util.UUID;

//I have no idea why for chests the Container class was not public and there is
public class EnderTanks {

    public EnderTanks(){}

    @Nullable
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        Map<String, EnderContainer> dataForPlayer = EnderTanksAccess.getDatabase().row(playerUUID.toString());
        if(dataForPlayer.isEmpty()) return null;

        CompoundNBT fileNBT = new CompoundNBT();

        for (Map.Entry<String, EnderContainer> stringEnderContainerEntry : dataForPlayer.entrySet()) {
            String code = stringEnderContainerEntry.getKey();
            EnderContainer enderContainer = stringEnderContainerEntry.getValue();
            fileNBT.put(code, enderContainer.save());
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream saveTo = new BufferedOutputStream(bos)) {
            CompressedStreamTools.writeCompressed(fileNBT, saveTo);
        }
        return bos;
    }

    public void loadSaveData(UUID playerUUID, InputStream in) throws Exception {
        if(in == null) return;
        CompoundNBT tag = CompressedStreamTools.readCompressed(in);

        for (String code : tag.getAllKeys()) {
            EnderContainer container = new EnderContainer(playerUUID.toString(), code);
            container.load(tag.getCompound(code));
            EnderTanksAccess.getDatabase().put(playerUUID.toString(), code, container);
        }
        in.close();
    }

    public void cleanup(UUID playerUUID) {
        EnderTanksAccess.getDatabase().row(playerUUID.toString()).clear();
    }
}