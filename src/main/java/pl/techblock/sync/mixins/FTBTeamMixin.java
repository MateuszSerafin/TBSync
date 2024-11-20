package pl.techblock.sync.mixins;

import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamBase;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.logic.mods.players.FTBParty;
import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(Team.class)
public abstract class FTBTeamMixin extends TeamBase {

    @Inject(method = "changedTeam", at = @At("HEAD"), remap = false)
    protected void onChangedTeam(@Nullable Team prev, UUID player, @Nullable ServerPlayerEntity p, boolean deleted, CallbackInfo ci) {
        if(deleted){
            try {
                DBManager.delete(player.toString(), FTBParty.teamMapTable);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("If you see this most likely ftbteams will be corrupted for %s", player));
            }
        }
    }
}
