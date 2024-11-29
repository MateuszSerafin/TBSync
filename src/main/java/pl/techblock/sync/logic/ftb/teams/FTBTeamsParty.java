package pl.techblock.sync.logic.ftb.teams;

import dev.ftb.mods.ftbteams.data.*;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.PartyPlayer;
import pl.techblock.sync.api.interfaces.IPartySync;

import java.util.List;
import java.util.UUID;

public class FTBTeamsParty implements IPartySync {

    private IFTBTeamsCustom giveInstance(){
        return (IFTBTeamsCustom) TeamManager.INSTANCE;
    }

    @Override
    public void saveParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        return;
    }

    private PlayerTeam createPlayerteam(PartyPlayer player){
        PlayerTeam team = new PlayerTeam(TeamManager.INSTANCE);
        ((IFTBTeamBaseCustom) team).setUUID(player.playerUUID());
        team.playerName = player.playerName();
        giveInstance().teamMap().put(player.playerUUID(), team);
        giveInstance().knownPlayers().put(player.playerUUID(), team);
        team.setProperty(Team.DISPLAY_NAME, team.playerName);
        team.setProperty(Team.COLOR, FTBTUtils.randomColor());
        ((IFTBTeamBaseCustom) team).getRanks().put(player.playerUUID(), TeamRank.OWNER);
        return team;
    }

    @Override
    public void loadParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        PartyTeam team = new PartyTeam(TeamManager.INSTANCE);
        ((IFTBTeamBaseCustom) team).setUUID(partyUUID);
        ((IFTBPartyTeamCustom) team).setOwner(owner.playerUUID());
        giveInstance().teamMap().put(partyUUID, team);
        team.setProperty(Team.DISPLAY_NAME, String.format("Drużyna gracza %s", owner.playerName()));
        team.setProperty(Team.COLOR, FTBTUtils.randomColor());

        PlayerTeam ownerPteam = giveInstance().knownPlayers().get(owner);
        if (ownerPteam == null) {
            ownerPteam = createPlayerteam(owner);
        }
        ownerPteam.actualTeam = team;
        ((IFTBTeamBaseCustom) ownerPteam).getRanks().remove(owner.playerUUID());
        ((IFTBTeamBaseCustom) team).getRanks().put(owner.playerUUID(), TeamRank.OWNER);

        for (PartyPlayer member : members) {
            PlayerTeam pteam = giveInstance().knownPlayers().get(member);
            if (pteam == null) {
                pteam = createPlayerteam(member);
            }
            pteam.actualTeam = team;
            ((IFTBTeamBaseCustom) pteam).getRanks().remove(member.playerUUID());
            ((IFTBTeamBaseCustom) team).getRanks().put(member.playerUUID(), TeamRank.MEMBER);
        }
    }

    private void cleanupPlayer(PartyPlayer player){
        giveInstance().teamMap().remove(player.playerUUID());
        giveInstance().knownPlayers().remove(player.playerUUID());
    }

    @Override
    public void cleanupParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        giveInstance().teamMap().remove(partyUUID);
        cleanupPlayer(owner);
        for (PartyPlayer member : members) {
            cleanupPlayer(member);
        }
    }

    @Override
    public void addMember(UUID partyUUID, PartyPlayer who) throws Exception {
        Team party = giveInstance().teamMap().get(partyUUID);
        if(party == null){
            TBSync.getLOGGER().error(String.format("Tried to add member for ftb party team where it does not exist %s", partyUUID));
            return;
        }
        ((IFTBTeamBaseCustom) party).getRanks().put(who.playerUUID(), TeamRank.MEMBER);
        PlayerTeam pteam = giveInstance().knownPlayers().get(who.playerUUID());
        if (pteam == null) {
            pteam = createPlayerteam(who);
        }
        pteam.actualTeam = party;
        ((IFTBTeamBaseCustom) pteam).getRanks().remove(who.playerUUID());
        TeamManager.INSTANCE.syncAll();
    }

    @Override
    public void removeMember(UUID partyUUID, PartyPlayer who) throws Exception {
        Team party = giveInstance().teamMap().get(partyUUID);
        if(party == null){
            TBSync.getLOGGER().error(String.format("Tried to remove member for ftb party team where it does not exist %s", partyUUID));
            return;
        }
        ((IFTBTeamBaseCustom) party).getRanks().remove(who.playerUUID());
        cleanupPlayer(who);
        createPlayerteam(who);
        TeamManager.INSTANCE.syncAll();
    }
}
