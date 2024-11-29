package pl.techblock.sync.logic.fluxnetworks;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundNBT;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import java.util.List;
import java.util.UUID;

public interface IFluxNetworksCustom {
    public void readCustom(CompoundNBT nbt);
    public CompoundNBT writeCustom(UUID pUUID);
    public void addStaticNetworkIDS(UUID pUUID, List<Integer> ids);
    public Int2ObjectMap<IFluxNetwork> getNetworks();
}