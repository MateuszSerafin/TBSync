package pl.techblock.sync.mixins.ftb.teams;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.techblock.sync.logic.ftb.teams.IFTBPartyTeamCustom;
import java.util.Collection;
import java.util.UUID;

@Mixin(PartyTeam.class)
public abstract class FTBPartyTeamMixin extends Team implements IFTBPartyTeamCustom {

    public FTBPartyTeamMixin(TeamManager m) {
        super(m);
    }

    @Shadow
    UUID owner;

    @Override
    public void setOwner(UUID uuid) {
        owner = uuid;
    }

    //this synchronization replaces need for invites, joins kicks etc everythings is managed by our islands
    @Inject(method = "transferOwnership", at = @At(value = "HEAD"), remap = false)
    public void onTransferOwnership(ServerPlayerEntity from, ServerPlayerEntity to, CallbackInfoReturnable<Integer> ci){
        from.sendMessage(new StringTextComponent("Nie mozesz tego tak zrobic"), Util.NIL_UUID);
        ci.setReturnValue(0);
        ci.cancel();
    }

    @Inject(method = "leave", at = @At(value = "HEAD"), remap = false)
    public void onLeave(ServerPlayerEntity player, CallbackInfoReturnable<Integer> ci){
        player.sendMessage(new StringTextComponent("Musisz opuscic wyspe aby to zrobic"), Util.NIL_UUID);
        ci.setReturnValue(0);
        ci.cancel();
    }

    @Inject(method = "invite", at = @At(value = "HEAD"), remap = false)
    public void onInvite(ServerPlayerEntity from, Collection<GameProfile> players, CallbackInfoReturnable<Integer> ci){
        from.sendMessage(new StringTextComponent("Jezeli gracz dolaczy do twojej wyspy zostanie dodany automatycznie"), Util.NIL_UUID);
        ci.setReturnValue(0);
        ci.cancel();
    }

    @Inject(method = "kick", at = @At(value = "HEAD"), remap = false)
    public void onKick(ServerPlayerEntity from, Collection<GameProfile> players, CallbackInfoReturnable<Integer> ci){
        from.sendMessage(new StringTextComponent("Jezeli usuniesz gracza z wyspy stanie sie to automatycznie"), Util.NIL_UUID);
        ci.setReturnValue(0);
        ci.cancel();
    }
}
