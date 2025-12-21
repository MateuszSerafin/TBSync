package pl.techblock.sync.logic.ftb.teams;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamRank;
import dev.ftb.mods.ftbteams.api.property.TeamProperties;
import dev.ftb.mods.ftbteams.data.*;
import org.jetbrains.annotations.Nullable;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.TBSyncConfig;
import pl.techblock.sync.api.IPartySync;
import pl.techblock.sync.api.PartyPlayer;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class FTBTeamsParty implements IPartySync {

    //this one is a funny one it actually kinda does nothing just loads data
    @Override
    public void savePartyToDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        return;
    }

    @Override
    public void loadPartyFromDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        loadParty(partyUUID, owner, members, null);
    }

    @Override
    public void cleanUpParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        getInstance().teamMap().remove(partyUUID);
        cleanupPlayer(owner);
        for (PartyPlayer member : members) {
            cleanupPlayer(member);
        }
    }

    private void cleanupPlayer(PartyPlayer player){
        getInstance().teamMap().remove(player.playerUUID());
        getInstance().knownPlayers().remove(player.playerUUID());
    }


    private PlayerTeam createPlayerteam(PartyPlayer player){
        PlayerTeam team = new PlayerTeam(TeamManagerImpl.INSTANCE, player.playerUUID());
        team.setPlayerName(player.playerName());
        getInstance().teamMap().put(player.playerUUID(), team);
        getInstance().knownPlayers().put(player.playerUUID(), team);
        team.setProperty(TeamProperties.DISPLAY_NAME, team.getPlayerName());
        team.setProperty(TeamProperties.COLOR, FTBTUtils.randomColor());
        ((IFTBTeamBaseCustom) team).getRanks().put(player.playerUUID(), TeamRank.OWNER);
        return team;
    }

    @Override
    public @Nullable ByteArrayOutputStream saveParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        //this one is a funny one it actually kinda does nothing just loads data
        return null;
    }

    @Override
    public void loadParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members, @Nullable InputStream data) {
        PartyTeam team = new PartyTeam(TeamManagerImpl.INSTANCE, partyUUID);
        ((IFTBPartyTeamCustom) team).setOwner(owner.playerUUID());
        getInstance().teamMap().put(partyUUID, team);
        team.setProperty(TeamProperties.DISPLAY_NAME, String.format(TBSyncConfig.locales.get("FTBTeamNames"), owner.playerName()));
        team.setProperty(TeamProperties.COLOR, FTBTUtils.randomColor());

        PlayerTeam ownerPteam = getInstance().knownPlayers().get(owner);
        if (ownerPteam == null) {
            ownerPteam = createPlayerteam(owner);
        }
        ownerPteam.setEffectiveTeam(team);
        ((IFTBTeamBaseCustom) ownerPteam).getRanks().remove(owner.playerUUID());
        ((IFTBTeamBaseCustom) team).getRanks().put(owner.playerUUID(), TeamRank.OWNER);

        for (PartyPlayer member : members) {
            PlayerTeam pteam = getInstance().knownPlayers().get(member);
            if (pteam == null) {
                pteam = createPlayerteam(member);
            }
            pteam.setEffectiveTeam(team);
            ((IFTBTeamBaseCustom) pteam).getRanks().remove(member.playerUUID());
            ((IFTBTeamBaseCustom) team).getRanks().put(member.playerUUID(), TeamRank.MEMBER);
        }
    }

    public void addMember(UUID partyUUID, PartyPlayer who) {
        AbstractTeam party = getInstance().teamMap().get(partyUUID);
        if(party == null){
            TBSync.getLogger().error("Tried to add member for ftb party team where it does not exist {} Player-UUID {}", partyUUID.toString(), who.playerUUID().toString());
            return;
        }
        ((IFTBTeamBaseCustom) party).getRanks().put(who.playerUUID(), TeamRank.MEMBER);
        PlayerTeam pteam = getInstance().knownPlayers().get(who.playerUUID());
        if (pteam == null) {
            pteam = createPlayerteam(who);
        }
        pteam.setEffectiveTeam(party);
        ((IFTBTeamBaseCustom) pteam).getRanks().remove(who.playerUUID());
        TeamManagerImpl.INSTANCE.syncToAll(party);
    }

    public void removeMember(UUID partyUUID, PartyPlayer who) {
        Team party = getInstance().teamMap().get(partyUUID);
        if(party == null){
            TBSync.getLogger().error("Tried to remove member for ftb party team where it does not exist {} Player-UUID {}", partyUUID.toString(), who.playerUUID().toString());
            return;
        }
        ((IFTBTeamBaseCustom) party).getRanks().remove(who.playerUUID());
        cleanupPlayer(who);
        createPlayerteam(who);
        TeamManagerImpl.INSTANCE.syncToAll(party);
    }

    private IFTBTeamsCustom getInstance(){
        return (IFTBTeamsCustom) TeamManagerImpl.INSTANCE;
    }
}