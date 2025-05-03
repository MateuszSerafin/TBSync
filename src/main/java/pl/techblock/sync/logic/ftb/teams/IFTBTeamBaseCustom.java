package pl.techblock.sync.logic.ftb.teams;

import dev.ftb.mods.ftbteams.api.TeamRank;
import java.util.Map;
import java.util.UUID;

public interface IFTBTeamBaseCustom {
    @Deprecated
    public void setUUID(UUID id);
    public Map<UUID, TeamRank> getRanks();
}