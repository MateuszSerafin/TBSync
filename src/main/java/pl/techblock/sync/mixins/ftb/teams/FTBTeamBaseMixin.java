package pl.techblock.sync.mixins.ftb.teams;

import dev.ftb.mods.ftbteams.api.TeamRank;
import dev.ftb.mods.ftbteams.data.AbstractTeamBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.ftb.teams.IFTBTeamBaseCustom;
import java.util.Map;
import java.util.UUID;

@Mixin(AbstractTeamBase.class)
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