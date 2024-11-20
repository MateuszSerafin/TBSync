package pl.techblock.sync.mixins;

import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.logic.PlayerManager;
import pl.techblock.sync.logic.enums.PlayerSync;
import pl.techblock.sync.logic.mods.players.FTBParty;

//without it players could duplicate rewards
@Mixin(PartyTeam.class)
public abstract class FTBPartyTeamMixin extends Team {

    public FTBPartyTeamMixin(TeamManager m) {
        super(m);
    }

    @Inject(method = "transferOwnership", at = @At(value = "RETURN", ordinal = 1), remap = false)
    public void onTransferOwnership(ServerPlayerEntity from, ServerPlayerEntity to, CallbackInfoReturnable<Integer> ci){
        try {
            DBManager.delete(from.getUUID().toString(), FTBParty.teamMapTable);
            PlayerManager.getSpecific(PlayerSync.FTBParty).saveToDB(to.getUUID());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
