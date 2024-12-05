package pl.techblock.sync.logic.ftb.quests;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.PartyPlayer;
import pl.techblock.sync.api.interfaces.IPartySync;
import pl.techblock.sync.db.DBManager;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.Blob;
import java.util.List;
import java.util.UUID;

//we synchronize only parties fully skip PlayerTeams, ServerTeams
public class FTBQuests implements IPartySync {

    private String tableName = "FTBQuests";

    public FTBQuests(){
        DBManager.createTable(tableName);
    };

    private IFTBQuestsFileCustom getServerQuests(){
        return (IFTBQuestsFileCustom) ServerQuestFile.INSTANCE;
    }

    @Nullable
    @Override
    public ByteArrayOutputStream getSavePartyData(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        TeamData data = getServerQuests().getTeamDataMap().get(partyUUID);
        if (data == null) return null;

        CompoundNBT tag = data.serializeNBT();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream saveTo = new BufferedOutputStream(bos)) {
            CompressedStreamTools.writeCompressed(tag, saveTo);
        }
        return bos;
    }

    @Override
    public void savePartyToDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        try {
            ByteArrayOutputStream bos = getSavePartyData(partyUUID, owner, members);
            if(bos == null) return;
            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsertBlob(partyUUID.toString(), tableName, bis);
            bis.close();
        } catch (Exception e){
            TBSync.getLOGGER().error(String.format("Something died while saving data for %s FTBQuests", partyUUID.toString()));
            e.printStackTrace();
        }
    }

    @Override
    public void loadPartyData(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members, InputStream in) throws Exception {
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

    @Override
    public void loadPartyFromDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        try {
            Blob compressedNBT = DBManager.selectBlob(partyUUID.toString(), tableName);
            if(compressedNBT == null) {
                loadPartyData(partyUUID, owner, members, null);
                return;
            }
            else {
                loadPartyData(partyUUID, owner, members, compressedNBT.getBinaryStream());
            }
            compressedNBT.free();
        } catch (Exception e){
            TBSync.getLOGGER().error(String.format("Something died while loading data for %s FTBQuests", partyUUID.toString()));
            e.printStackTrace();
        }
    }

    @Override
    public void cleanupParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        getServerQuests().getTeamDataMap().remove(partyUUID);
    }

    //n/a
    @Override
    public void addMember(UUID partyUUID, PartyPlayer who) throws Exception {

    }

    //n/a
    @Override
    public void removeMember(UUID partyUUID, PartyPlayer who) throws Exception {

    }
}
