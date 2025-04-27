package pl.techblock.sync.logic.astral;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.mixins.astral.AstralResearchAccess;
import javax.annotation.Nullable;
import java.io.*;
import java.util.UUID;

public class AstralResearch {

    public AstralResearch(){}

    @Nullable
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        CompoundNBT tag = new CompoundNBT();
        //getProgress always returns something no chance for null
        AstralResearchAccess.getProgress(playerUUID).store(tag);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream saveTo = new BufferedOutputStream(bos)) {
            CompressedStreamTools.writeCompressed(tag, saveTo);
        }
        return bos;
    }

    public void loadSaveData(UUID playerUUID, @Nullable InputStream in) throws Exception {
        if(in == null) return;
        CompoundNBT tag = CompressedStreamTools.readCompressed(in);
        AstralResearchAccess.load_unsafeFromNBT(playerUUID, tag);
        in.close();
    }

    public void cleanup(UUID playerUUID) {
        //I would need like 5 mixins to do it correctly, if that will cause issues will fix
        AstralResearchAccess.getProgress().remove(playerUUID);
    }
}