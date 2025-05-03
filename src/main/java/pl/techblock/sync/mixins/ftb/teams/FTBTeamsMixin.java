package pl.techblock.sync.mixins.ftb.teams;

import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.data.AbstractTeam;
import dev.ftb.mods.ftbteams.data.PlayerTeam;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.ftb.teams.IFTBTeamsCustom;
import java.util.Map;
import java.util.UUID;

@Mixin(TeamManagerImpl.class)
public abstract class FTBTeamsMixin implements IFTBTeamsCustom {

    @Shadow
    @Final
    Map<UUID, PlayerTeam> knownPlayers;

    @Shadow
    @Final
    Map<UUID, AbstractTeam> teamMap;

    //checked it doesn't matter if it's duplicated
    @Shadow
    Map<String, Team> nameMap;

    @Override
    public Map<UUID, PlayerTeam> knownPlayers() {
        return knownPlayers;
    }

    @Override
    public Map<UUID, AbstractTeam> teamMap() {
        return teamMap;
    }

    @Override
    public Map<String, Team> nameMap() {
        return nameMap;
    }

    @Overwrite(remap = false)
    public void load(){
        return;
    }

    @Overwrite(remap = false)
    public void saveNow() {
        return;
    }
}