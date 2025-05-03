package pl.techblock.sync.logic.ftb.teams;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.TeamRank;
import dev.ftb.mods.ftbteams.api.property.TeamProperties;
import dev.ftb.mods.ftbteams.data.*;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.utils.PartyPlayer;
import java.util.List;
import java.util.UUID;

public class FTBTeamsParty {

    private IFTBTeamsCustom giveInstance(){
        return (IFTBTeamsCustom) TeamManagerImpl.INSTANCE;
    }

    private PlayerTeam createPlayerteam(PartyPlayer player){
        PlayerTeam team = new PlayerTeam(TeamManagerImpl.INSTANCE, player.playerUUID());
        team.setPlayerName(player.playerName());
        giveInstance().teamMap().put(player.playerUUID(), team);
        giveInstance().knownPlayers().put(player.playerUUID(), team);
        team.setProperty(TeamProperties.DISPLAY_NAME, team.getPlayerName());
        team.setProperty(TeamProperties.COLOR, FTBTUtils.randomColor());
        ((IFTBTeamBaseCustom) team).getRanks().put(player.playerUUID(), TeamRank.OWNER);
        return team;
    }

    //this one is a funny one it actually kinda does nothing just loads data
    public void loadPartyData(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        PartyTeam team = new PartyTeam(TeamManagerImpl.INSTANCE, partyUUID);
        ((IFTBPartyTeamCustom) team).setOwner(owner.playerUUID());
        giveInstance().teamMap().put(partyUUID, team);
        team.setProperty(TeamProperties.DISPLAY_NAME, String.format("Drużyna gracza %s", owner.playerName()));
        team.setProperty(TeamProperties.COLOR, FTBTUtils.randomColor());

        PlayerTeam ownerPteam = giveInstance().knownPlayers().get(owner);
        if (ownerPteam == null) {
            ownerPteam = createPlayerteam(owner);
        }
        ownerPteam.setEffectiveTeam(team);
        ((IFTBTeamBaseCustom) ownerPteam).getRanks().remove(owner.playerUUID());
        ((IFTBTeamBaseCustom) team).getRanks().put(owner.playerUUID(), TeamRank.OWNER);

        for (PartyPlayer member : members) {
            PlayerTeam pteam = giveInstance().knownPlayers().get(member);
            if (pteam == null) {
                pteam = createPlayerteam(member);
            }
            pteam.setEffectiveTeam(team);
            ((IFTBTeamBaseCustom) pteam).getRanks().remove(member.playerUUID());
            ((IFTBTeamBaseCustom) team).getRanks().put(member.playerUUID(), TeamRank.MEMBER);
        }
    }

    private void cleanupPlayer(PartyPlayer player){
        giveInstance().teamMap().remove(player.playerUUID());
        giveInstance().knownPlayers().remove(player.playerUUID());
    }

    public void cleanupParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        giveInstance().teamMap().remove(partyUUID);
        cleanupPlayer(owner);
        for (PartyPlayer member : members) {
            cleanupPlayer(member);
        }
    }

    public void addMember(UUID partyUUID, PartyPlayer who) throws Exception {
        AbstractTeam party = giveInstance().teamMap().get(partyUUID);
        if(party == null){
            TBSync.getLOGGER().error(String.format("Tried to add member for ftb party team where it does not exist %s", partyUUID));
            return;
        }
        ((IFTBTeamBaseCustom) party).getRanks().put(who.playerUUID(), TeamRank.MEMBER);
        PlayerTeam pteam = giveInstance().knownPlayers().get(who.playerUUID());
        if (pteam == null) {
            pteam = createPlayerteam(who);
        }
        pteam.setEffectiveTeam(party);
        ((IFTBTeamBaseCustom) pteam).getRanks().remove(who.playerUUID());
        TeamManagerImpl.INSTANCE.syncToAll(party);
    }

    public void removeMember(UUID partyUUID, PartyPlayer who) throws Exception {
        Team party = giveInstance().teamMap().get(partyUUID);
        if(party == null){
            TBSync.getLOGGER().error(String.format("Tried to remove member for ftb party team where it does not exist %s", partyUUID));
            return;
        }
        ((IFTBTeamBaseCustom) party).getRanks().remove(who.playerUUID());
        cleanupPlayer(who);
        createPlayerteam(who);
        TeamManagerImpl.INSTANCE.syncToAll(party);
    }
}