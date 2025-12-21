package pl.techblock.sync.logic.ftb.quests;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.IPartySync;
import pl.techblock.sync.api.PartyPlayer;
import pl.techblock.sync.db.DBManager;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.Blob;
import java.util.List;
import java.util.UUID;

//we synchronize only parties fully skip PlayerTeams, ServerTeams
public class FTBQuests implements IPartySync {

    private final String ftbQuestsTable = "FTBQuests";

    public FTBQuests(){
        DBManager.createTable(ftbQuestsTable);
    };

    @Override
    public void savePartyToDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        ByteArrayOutputStream data = saveParty(partyUUID, owner, members);
        if(data == null) return;
        DBManager.upsertBlob(partyUUID.toString(), ftbQuestsTable, new ByteArrayInputStream(data.toByteArray()));
    }

    @Override
    public void loadPartyFromDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        Blob data = DBManager.selectBlob(partyUUID.toString(), ftbQuestsTable);
        if(data == null){
            loadParty(partyUUID, owner, members, null);
            return;
        }
        try {
            loadParty(partyUUID, owner, members, data.getBinaryStream());
        } catch (Exception e){
            TBSync.getLogger().error("Caught error while loading data from database for FTQuests Party-UUID: {}", partyUUID.toString());
            TBSync.getLogger().error(e.getMessage());
        }
    }

    @Override
    public void cleanUpParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        getServerQuests().getTeamDataMap().remove(partyUUID);
    }

    @Nullable
    @Override
    public ByteArrayOutputStream saveParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        try {
            TeamData data = getServerQuests().getTeamDataMap().get(partyUUID);
            if (data == null) return null;

            CompoundTag tag = data.serializeNBT();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                NbtIo.writeCompressed(tag, saveTo);
            }
            return bos;
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught gathering data for FTBQuests, Party-UUID: {}", partyUUID.toString());
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }

    @Override
    public void loadParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members, @org.jetbrains.annotations.Nullable InputStream data) {
        try {
            if(data == null) {
                TeamData teamData = new TeamData(partyUUID, ServerQuestFile.INSTANCE);
                ((IFTBTeamDataCustom) teamData).setCreatedByMe();
                getServerQuests().getTeamDataMap().put(partyUUID, teamData);
                return;
            }
            CompoundTag nbt = NbtIo.readCompressed(data, NbtAccounter.unlimitedHeap());
            data.close();
            TeamData teamData = new TeamData(partyUUID, ServerQuestFile.INSTANCE);
            teamData.deserializeNBT(SNBTCompoundTag.of(nbt));
            ((IFTBTeamDataCustom) teamData).setCreatedByMe();
            getServerQuests().getTeamDataMap().put(partyUUID, teamData);
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught loading data for FTBQuests, Party-UUID: {}", partyUUID.toString());
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }

    @Override
    public void addMember(UUID partyUUID, PartyPlayer who) {
        //N/A
        return;
    }

    @Override
    public void removeMember(UUID partyUUID, PartyPlayer who) {
        //N/A
        return;
    }

    private IFTBQuestsFileCustom getServerQuests(){
        return (IFTBQuestsFileCustom) ServerQuestFile.INSTANCE;
    }
}