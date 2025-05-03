package pl.techblock.sync.logic.ftb.quests;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import javax.annotation.Nullable;
import java.io.*;
import java.util.UUID;

//we synchronize only parties fully skip PlayerTeams, ServerTeams
public class FTBQuests {

    public FTBQuests(){};

    private IFTBQuestsFileCustom getServerQuests(){
        return (IFTBQuestsFileCustom) ServerQuestFile.INSTANCE;
    }

    @Nullable
    public ByteArrayOutputStream getSavePartyData(UUID partyUUID) throws Exception {
        TeamData data = getServerQuests().getTeamDataMap().get(partyUUID);
        if (data == null) return null;

        CompoundTag tag = data.serializeNBT();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream saveTo = new BufferedOutputStream(bos)) {
            NbtIo.writeCompressed(tag, saveTo);
        }
        return bos;
    }

    public void loadPartyData(UUID partyUUID, InputStream in) throws Exception {
        if(in == null) {
            TeamData data = new TeamData(partyUUID, ServerQuestFile.INSTANCE);
            ((IFTBTeamDataCustom) data).setCreatedByMe();
            getServerQuests().getTeamDataMap().put(partyUUID, data);
            return;
        }
        CompoundTag nbt = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
        in.close();
        TeamData data = new TeamData(partyUUID, ServerQuestFile.INSTANCE);
        data.deserializeNBT(SNBTCompoundTag.of(nbt));
        ((IFTBTeamDataCustom) data).setCreatedByMe();
        getServerQuests().getTeamDataMap().put(partyUUID, data);
    }

    public void cleanupParty(UUID partyUUID) {
        getServerQuests().getTeamDataMap().remove(partyUUID);
    }
}