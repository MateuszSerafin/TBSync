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

    @Override
    public void saveParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        try {
            TeamData data = getServerQuests().getTeamDataMap().get(partyUUID);
            if (data == null) return;

            CompoundNBT tag = data.serializeNBT();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                CompressedStreamTools.writeCompressed(tag, saveTo);
            }

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
    public void loadParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        //overwrite what is existing
        //stolen from ServerQuestFile
        //there is a mixin that prevents collecting rewards if Team was created by not me (such as on playerloggedin)
        try {
            Blob compressedNBT = DBManager.selectBlob(partyUUID.toString(), tableName);
            //can happen
            if(compressedNBT == null) {
                TeamData data = new TeamData(partyUUID);
                data.file = ServerQuestFile.INSTANCE;
                ((IFTBTeamDataCustom) data).setCreatedByMe();
                getServerQuests().getTeamDataMap().put(partyUUID, data);
                return;
            }

            InputStream stream = compressedNBT.getBinaryStream();
            CompoundNBT nbt = CompressedStreamTools.readCompressed(stream);
            stream.close();

            TeamData data = new TeamData(partyUUID);
            data.file = ServerQuestFile.INSTANCE;
            data.deserializeNBT(SNBTCompoundTag.of(nbt));
            ((IFTBTeamDataCustom) data).setCreatedByMe();
            getServerQuests().getTeamDataMap().put(partyUUID, data);
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
