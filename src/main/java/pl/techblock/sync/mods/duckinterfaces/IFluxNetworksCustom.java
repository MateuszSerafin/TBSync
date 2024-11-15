package pl.techblock.sync.mods.duckinterfaces;

import net.minecraft.nbt.CompoundNBT;
import java.util.List;
import java.util.UUID;

public interface IFluxNetworksCustom {
    public void readCustom(CompoundNBT nbt);
    public CompoundNBT writeCustom(UUID pUUID);
    public void addStaticNetworkIDS(UUID pUUID, List<Integer> ids);
}
