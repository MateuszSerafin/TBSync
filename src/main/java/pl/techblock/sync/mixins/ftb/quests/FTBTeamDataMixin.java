package pl.techblock.sync.mixins.ftb.quests;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.techblock.sync.logic.ftb.quests.IFTBTeamDataCustom;
import java.util.UUID;

@Mixin(TeamData.class)
public abstract class FTBTeamDataMixin implements IFTBTeamDataCustom {

    @Unique
    private boolean wasCreatedByMe = false;

    @Override
    public void setCreatedByMe() {
        wasCreatedByMe = true;
    }

    @Inject(method = "claimReward", at = @At("HEAD"), remap = false, cancellable = true)
    public void onClaimReward(UUID player, Reward reward, long date, CallbackInfoReturnable<Boolean> cir) {
        if(!this.wasCreatedByMe){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}