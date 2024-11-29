package pl.techblock.sync.mixins.cosmeticarmor;

import com.google.common.cache.LoadingCache;
import lain.mods.cos.impl.InventoryManager;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import lain.mods.cos.impl.network.packet.PacketSyncCosArmor;
import lain.mods.cos.impl.network.packet.PacketSyncHiddenFlags;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.LogicalSidedProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.cosmeticarmor.ICosmeticArmorCustom;
import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(InventoryManager.class)
public abstract class CosmeticArmorMixin implements ICosmeticArmorCustom {

    @Shadow
    public InventoryCosArmor getCosArmorInventory(UUID uuid) {
        return null;
    }

    @Shadow
    @Final
    protected LoadingCache<UUID, InventoryCosArmor> CommonCache;


    @Overwrite(remap = false)
    private void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        //CommonCache.invalidate(event.getPlayer().getUUID());
        getCosArmorInventory(event.getPlayer().getUUID());

        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            for (ServerPlayerEntity other : LogicalSidedProvider.INSTANCE.<MinecraftServer>get(LogicalSide.SERVER).getPlayerList().getPlayers()) {
                if (other == player)
                    continue;
                UUID uuid = other.getUUID();
                InventoryCosArmor inv = getCosArmorInventory(uuid);
                for (int i = 0; i < inv.getSlots(); i++)
                    ModObjects.network.sendTo(new PacketSyncCosArmor(uuid, inv, i), player);
                inv.forEachHidden((modid, identifier) -> ModObjects.network.sendTo(new PacketSyncHiddenFlags(uuid, inv, modid, identifier), player));
            }
        }
    }

    @Overwrite(remap = false)
    private void handlePlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        //all it did is save and clean cache
        //we do it through our custom event
        return;
    }


    @Overwrite(remap = false)
    protected void loadInventory(UUID uuid, InventoryCosArmor inventory) {
        return;
    }

    @Overwrite(remap = false)
    protected void saveInventory(UUID uuid, InventoryCosArmor inventory) {
        return;
    }


    @Override
    public void readCustom(UUID playerUUID, @Nullable CompoundNBT nbt) {
        InventoryCosArmor armor = CommonCache.getUnchecked(playerUUID);
        if(nbt != null){
            armor.deserializeNBT(nbt);
        }
    }

    @Override
    @Nullable
    public CompoundNBT writeCustom(UUID pUUID) {
        InventoryCosArmor armor = CommonCache.getIfPresent(pUUID);
        //yea special case mod doesn't handle empty nbt tags
        //if its null i won't save to db
        if(armor == null){
            return null;
        }
        return armor.serializeNBT();
    }

    @Override
    public void invalidate(UUID playerUUID) {
        CommonCache.invalidate(playerUUID);
    }
}
