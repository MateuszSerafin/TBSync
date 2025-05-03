package pl.techblock.sync.mixins.ftb.teams;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbteams.data.AbstractTeam;
import dev.ftb.mods.ftbteams.data.PartyTeam;
import dev.ftb.mods.ftbteams.data.TeamManagerImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.techblock.sync.logic.ftb.teams.IFTBPartyTeamCustom;
import java.util.Collection;
import java.util.UUID;

@Mixin(PartyTeam.class)
public abstract class FTBPartyTeamMixin extends AbstractTeam implements IFTBPartyTeamCustom {

    public FTBPartyTeamMixin(TeamManagerImpl manager, UUID id) {
        super(manager, id);
    }

    @Shadow
    UUID owner;

    @Override
    public void setOwner(UUID uuid) {
        owner = uuid;
    }

    //this synchronization replaces need for invites, joins kicks etc everythings is managed by our islands
    @Inject(method = "transferOwnership", at = @At(value = "HEAD"), remap = false)
    public void onTransferOwnership(CommandSourceStack from, Collection<GameProfile> toProfiles, CallbackInfoReturnable<Integer> ci) {
        from.sendChatMessage(OutgoingChatMessage.create(PlayerChatMessage.unsigned(from.getPlayer().getUUID(), "Nie mozesz tego zrobic")), false, ChatType.bind(ChatType.CHAT, from.getPlayer()));
        ci.setReturnValue(0);
        ci.cancel();
    }

    @Inject(method = "leave", at = @At(value = "HEAD"), remap = false)
    public void onLeave(UUID id, CallbackInfoReturnable<Integer> ci){
        ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(id);
        if(player != null){
            player.sendChatMessage(OutgoingChatMessage.create(PlayerChatMessage.unsigned(player.getUUID(), "Musisz opuscic wyspe aby to zrobic")), false, ChatType.bind(ChatType.CHAT, player));
        }
        ci.setReturnValue(0);
        ci.cancel();
    }

    @Inject(method = "invite", at = @At(value = "HEAD"), remap = false)
    public void onInvite(ServerPlayer inviter, Collection<GameProfile> profiles, CallbackInfoReturnable<Integer> ci){
        inviter.sendChatMessage(OutgoingChatMessage.create(PlayerChatMessage.unsigned(inviter.getUUID(), "Jezeli gracz dolaczy do twojej wyspy zostanie dodany automatycznie")), false, ChatType.bind(ChatType.CHAT, inviter));
        ci.setReturnValue(0);
        ci.cancel();
    }

    @Inject(method = "kick", at = @At(value = "HEAD"), remap = false)
    public void onKick(CommandSourceStack from, Collection<GameProfile> players, CallbackInfoReturnable<Integer> ci){
        from.sendChatMessage(OutgoingChatMessage.create(PlayerChatMessage.unsigned(from.getPlayer().getUUID(), "Jezeli usuniesz gracza z wyspy stanie sie to automatycznie")), false, ChatType.bind(ChatType.CHAT, from));
        ci.setReturnValue(0);
        ci.cancel();
    }
}