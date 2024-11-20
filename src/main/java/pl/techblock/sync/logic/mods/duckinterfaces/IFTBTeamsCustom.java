package pl.techblock.sync.logic.mods.duckinterfaces;

import dev.ftb.mods.ftbteams.data.PlayerTeam;
import dev.ftb.mods.ftbteams.data.Team;
import java.util.Map;
import java.util.UUID;

public interface IFTBTeamsCustom {
    public Map<UUID, PlayerTeam> knownPlayers();
    public Map<UUID, Team> teamMap();
    public Map<String, Team> nameMap();
}
