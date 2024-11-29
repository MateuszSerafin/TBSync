package pl.techblock.sync.logic.cosmeticarmor;

import net.minecraft.nbt.CompoundNBT;
import java.util.UUID;

public interface ICosmeticArmorCustom {
    public void readCustom(UUID playerUUID, CompoundNBT nbt);
    public CompoundNBT writeCustom(UUID pUUID);
    public void invalidate(UUID playerUUID);
}
