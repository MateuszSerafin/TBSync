package pl.techblock.sync.logic.ftb.teams;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbteams.data.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.PlayerManager;
import pl.techblock.sync.api.enums.PlayerSync;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.logic.mods.duckinterfaces.IFTBTeamBaseCustom;
import pl.techblock.sync.logic.mods.duckinterfaces.IFTBTeamsCustom;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.Blob;
import java.util.Map;
import java.util.UUID;

public class FTBParty implements IPlayerSync {

    public static String teamMapTable = "FTBTeamsMap";

    private IFTBTeamsCustom giveInstance(){
        return (IFTBTeamsCustom) TeamManager.INSTANCE;
    }

    public FTBParty(){}

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

    @Override
    public void saveToDB(UUID playerUUID) {
        try {
            PartyTeam ownerOf = checkIfPlayerIsOwnerOfATeam(playerUUID);

            if(ownerOf == null) return;

            SNBTCompoundTag tag = ownerOf.serializeNBT();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                CompressedStreamTools.writeCompressed(tag, out);
            } catch (Exception e){
                TBSync.getLOGGER().error("Compressing nbt died for FTBTeams");
                e.printStackTrace();
            }



            ByteArrayInputStream partyTeamBis = new ByteArrayInputStream(out.toByteArray());
            DBManager.upsert(playerUUID.toString(), teamMapTable, partyTeamBis);
            partyTeamBis.close();

            //i might as well save player PlayerTeam it literary does not save any data on this team it self
            //everything has to be loaded, but there is this scenario where in DB Playerteam doesn't exist and it will not work for player
            //work around is dirty mixin but i think this will resolve it
            //This causes DB to save multiple times now and then later when there is call for each player
            for (UUID member : ownerOf.getMembers()) {
                PlayerManager.getSpecific(PlayerSync.FTBKnownPlayer).saveToDB(member);
            }
        }
        catch (Exception e){
            TBSync.getLOGGER().error("Something with FTB Party saving data");
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        try {
            Blob partyInfo = DBManager.select(playerUUID.toString(), teamMapTable);
            if(partyInfo == null){
                //not owner of any party
                return;
            }
            InputStream partyStream = partyInfo.getBinaryStream();
            CompoundNBT nbt = CompressedStreamTools.readCompressed(partyStream);
            UUID teamUUID = UUID.fromString(nbt.getString("id"));
            PartyTeam team = new PartyTeam(TeamManager.INSTANCE);
            team.deserializeNBT(nbt);
            ((IFTBTeamBaseCustom) team).setUUID(teamUUID);
            giveInstance().teamMap().put(teamUUID, team);

            //it can go wrong i am leaving MIXIN for it that is in active for future will see in testing if it will cause issues
            //theoretically should not
            for (UUID member : team.getMembers()) {
                PlayerManager.getSpecific(PlayerSync.FTBKnownPlayer).loadFromDB(member);

                PlayerTeam pteam = giveInstance().knownPlayers().get(member);

                if(pteam == null){
                    TBSync.getLOGGER().error("I told it will be a problem issue with FTBParty");
                    continue;
                }

                //Snippet Stolen from TeamManager
                pteam.actualTeam = team;
                ((IFTBTeamBaseCustom) pteam).getRanks().remove(playerUUID);
                //we preload data before player on server not a problem
                //((PlayerTeam)pteam).updatePresence();
                //TeamManager.INSTANCE.syncAll();
            }

            partyStream.close();
            partyInfo.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with FTB Party while loading data");
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(UUID playerUUID) {
        PartyTeam team = checkIfPlayerIsOwnerOfATeam(playerUUID);
        if(team == null) return;
        giveInstance().teamMap().remove(team.getId());
        for (UUID member : team.getMembers()) {
            PlayerManager.getSpecific(PlayerSync.FTBKnownPlayer).cleanup(member);
        }
    }
}
