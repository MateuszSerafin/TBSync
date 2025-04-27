package pl.techblock.sync.logic.ftb.quests;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
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

        CompoundNBT tag = data.serializeNBT();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream saveTo = new BufferedOutputStream(bos)) {
            CompressedStreamTools.writeCompressed(tag, saveTo);
        }
        return bos;
    }

    public void loadPartyData(UUID partyUUID, InputStream in) throws Exception {
        if(in == null) {
            TeamData data = new TeamData(partyUUID);
            data.file = ServerQuestFile.INSTANCE;
            ((IFTBTeamDataCustom) data).setCreatedByMe();
            getServerQuests().getTeamDataMap().put(partyUUID, data);
            return;
        }
        CompoundNBT nbt = CompressedStreamTools.readCompressed(in);
        in.close();
        TeamData data = new TeamData(partyUUID);
        data.file = ServerQuestFile.INSTANCE;
        data.deserializeNBT(SNBTCompoundTag.of(nbt));
        ((IFTBTeamDataCustom) data).setCreatedByMe();
        getServerQuests().getTeamDataMap().put(partyUUID, data);
    }

    public void cleanupParty(UUID partyUUID) throws Exception {
        getServerQuests().getTeamDataMap().remove(partyUUID);
    }
}