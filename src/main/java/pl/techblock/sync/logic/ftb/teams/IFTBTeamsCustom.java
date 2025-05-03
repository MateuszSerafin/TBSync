package pl.techblock.sync.logic.ftb.teams;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.AbstractTeam;
import dev.ftb.mods.ftbteams.data.PlayerTeam;
import java.util.Map;
import java.util.UUID;

public interface IFTBTeamsCustom {
    public Map<UUID, PlayerTeam> knownPlayers();
    public Map<UUID, AbstractTeam> teamMap();
    //This should be used but is not lol.
    public Map<String, Team> nameMap();
}