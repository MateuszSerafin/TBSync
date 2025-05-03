package pl.techblock.sync.logic.fluxnetworks;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import sonar.fluxnetworks.common.connection.FluxNetwork;
import java.util.List;
import java.util.UUID;

public interface IFluxNetworksCustom {
    public void readCustom(CompoundTag nbt) throws Exception;
    public CompoundTag writeCustom(UUID pUUID);
    public void addStaticNetworkIDS(UUID pUUID, List<Integer> ids);
    public Int2ObjectMap<FluxNetwork> getNetworks();
}