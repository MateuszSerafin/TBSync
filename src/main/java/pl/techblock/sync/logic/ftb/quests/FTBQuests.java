package pl.techblock.sync.logic.ftb.quests;

import com.mojang.util.UUIDTypeAdapter;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.logic.mods.duckinterfaces.IFTBQuestsFileCustom;
import pl.techblock.sync.logic.mods.duckinterfaces.IFTBTeamDataCustom;
import pl.techblock.sync.logic.mods.duckinterfaces.IFTBTeamsCustom;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.Blob;
import java.util.Map;
import java.util.UUID;

public class FTBQuests implements IPlayerSync {
    //pretty sure there is advantage of not deleting quests from database
    //i would rather have more data than issue with players
    //it's beacuse its stored as teamuuid -> quests
    //team uuid will change to different if player changes team so it will be just rotting in database but at least we can recover that
    //there is chance of collision when player creates a team and it's already in database <- low chance

    public FTBQuests(){

    };

    private String tableName = "FTBQuests";



    private IFTBTeamsCustom giveInstance(){
        return (IFTBTeamsCustom) TeamManager.INSTANCE;
    }

    @Nullable
    private PartyTeam checkIfPlayerIsOwnerOfATeam(UUID playerUUID){
        for (Map.Entry<UUID, Team> uuidTeamEntry : giveInstance().teamMap().entrySet()) {
            Team team = uuidTeamEntry.getValue();
            if(!(team instanceof PartyTeam)) continue;
            if(!team.getOwner().equals(playerUUID)) continue;
            return (PartyTeam) team;
        }
        return null;
    }

    @Nullable
    private PartyTeam checkIfPlayerIsMemberOfPartyTeam(UUID playerUUID){
        for (Map.Entry<UUID, Team> uuidTeamEntry : giveInstance().teamMap().entrySet()) {
            Team team = uuidTeamEntry.getValue();
            if(!(team instanceof PartyTeam)) continue;
            for (UUID member : team.getMembers()) {
                if (member.equals(playerUUID)) return (PartyTeam) team;
            }
        }
        return null;
    }

    private IFTBQuestsFileCustom getServerQuests(){
        return (IFTBQuestsFileCustom) ServerQuestFile.INSTANCE;
    }

    private void loadTeamPlayerOrParty(UUID playerorparty){
        //overwrite what is existing
        //stolen from ServerQuestFile
        //there is a mixin that prevents collecting rewards if Team was created by not me (such as on playerloggedin)
        //tldr if player switches servers rewards cannot be collected but it means we need to create instance of TeamData
        //even if's only for player .actual team should override the TeamData and everything should be ok
        //this is like work around to work around (it becomes shit)
        //i don't think there are any issues with doing it this way (aside from storing more data)
        try {
            Blob compressedNBT = DBManager.select(playerorparty.toString(), tableName);
            //can happen
            if(compressedNBT == null) {
                TeamData data = new TeamData(playerorparty);
                data.file = ServerQuestFile.INSTANCE;
                ((IFTBTeamDataCustom) data).setCreatedByMe();
                getServerQuests().getTeamDataMap().put(playerorparty, data);
                return;
            }

            InputStream stream = compressedNBT.getBinaryStream();
            CompoundNBT nbt = CompressedStreamTools.readCompressed(stream);
            stream.close();

            UUID uuid = UUIDTypeAdapter.fromString(nbt.getString("uuid"));
            TeamData data = new TeamData(uuid);
            data.file = ServerQuestFile.INSTANCE;
            data.deserializeNBT(SNBTCompoundTag.of(nbt));
            ((IFTBTeamDataCustom) data).setCreatedByMe();
            getServerQuests().getTeamDataMap().put(uuid, data);
        } catch (Exception e){
            TBSync.getLOGGER().error(String.format("Something died while loading data for %s FTBQuests", playerorparty));
            e.printStackTrace();
        }
    }

    private void saveTeamPlayerOrParty(UUID playerorparty){
        try {
            TeamData data = getServerQuests().getTeamDataMap().get(playerorparty);
            if (data == null) return;

            CompoundNBT tag = data.serializeNBT();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                CompressedStreamTools.writeCompressed(tag, saveTo);
            }

            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsert(playerorparty.toString(), tableName, bis);
            bis.close();
        } catch (Exception e){
            TBSync.getLOGGER().error(String.format("Something died while saving data for %s FTBQuests", playerorparty));
            e.printStackTrace();
        }
    }

    private void cleanupTeamPlayerOrParty(UUID playerorparty){
        getServerQuests().getTeamDataMap().remove(playerorparty);
    }


    //if owner load party
    //if member of party don't load anything
    //if not above try loading regular id (PlayerTeam)
    @Override
    public void saveToDB(UUID playerUUID) {
        PartyTeam party = checkIfPlayerIsOwnerOfATeam(playerUUID);
        if(party != null){
            saveTeamPlayerOrParty(party.getId());
            return;
        }
        PartyTeam member = checkIfPlayerIsMemberOfPartyTeam(playerUUID);
        if(member != null) return;
        saveTeamPlayerOrParty(playerUUID);
    }

    //if owner load party
    //if member of party don't load anything
    //if not above try loading regular id (PlayerTeam)
    @Override
    public void loadFromDB(UUID playerUUID) {
        PartyTeam party = checkIfPlayerIsOwnerOfATeam(playerUUID);
        if(party != null){
            loadTeamPlayerOrParty(party.getId());
            return;
        }
        PartyTeam member = checkIfPlayerIsMemberOfPartyTeam(playerUUID);
        if(member != null) return;
        loadTeamPlayerOrParty(playerUUID);
        //chance of nothing being loaded
    }


    @Override
    public void cleanup(UUID playerUUID) {
        PartyTeam party = checkIfPlayerIsOwnerOfATeam(playerUUID);
        if(party != null){
            cleanupTeamPlayerOrParty(party.getId());
            return;
        }
        PartyTeam member = checkIfPlayerIsMemberOfPartyTeam(playerUUID);
        if(member != null) return;
        cleanupTeamPlayerOrParty(playerUUID);
    }
}
