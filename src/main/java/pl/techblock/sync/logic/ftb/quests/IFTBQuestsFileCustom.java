package pl.techblock.sync.logic.ftb.quests;

import dev.ftb.mods.ftbquests.quest.TeamData;
import java.util.Map;
import java.util.UUID;

public interface IFTBQuestsFileCustom {
    public Map<UUID, TeamData> getTeamDataMap();
}