package pl.techblock.sync.mixins.ftb;

import dev.ftb.mods.ftbteams.data.TeamBase;
import dev.ftb.mods.ftbteams.data.TeamRank;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.mods.duckinterfaces.IFTBTeamBaseCustom;
import java.util.Map;
import java.util.UUID;

@Mixin(TeamBase.class)
public abstract class FTBTeamBaseMixin implements IFTBTeamBaseCustom {
    @Shadow
    UUID id;

    @Shadow
    @Final
    Map<UUID, TeamRank> ranks;

    @Override
    public void setUUID(UUID id) {
        this.id = id;
    }

    public Map<UUID, TeamRank> getRanks(){
        return ranks;
    }


}
