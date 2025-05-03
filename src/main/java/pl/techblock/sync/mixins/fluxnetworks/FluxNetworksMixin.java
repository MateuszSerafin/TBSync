package pl.techblock.sync.mixins.fluxnetworks;

import com.google.gson.internal.LinkedTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.*;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.logic.fluxnetworks.IFluxNetworksCustom;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.api.network.SecurityLevel;
import sonar.fluxnetworks.common.connection.FluxNetwork;
import sonar.fluxnetworks.common.connection.FluxNetworkData;
import sonar.fluxnetworks.common.connection.ServerFluxNetwork;
import sonar.fluxnetworks.register.Channel;
import sonar.fluxnetworks.register.Messages;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(FluxNetworkData.class)
public abstract class FluxNetworksMixin extends SavedData implements IFluxNetworksCustom {

    @Shadow
    private final Int2ObjectOpenHashMap<FluxNetwork> mNetworks = new Int2ObjectOpenHashMap();

    @Unique
    public Map<UUID, List<Integer>> ListOfAvaiableNetworksForPlayer = new LinkedTreeMap<>();

    @Nullable
    @Overwrite(remap = false)
    public FluxNetwork createNetwork(@Nonnull ServerPlayer creator, @Nonnull String name, int color, @Nonnull SecurityLevel security, @Nonnull String password) throws Exception {

        //only time it would not be loaded is when player would be on other server visiting island
        UUID uuid = creator.getGameProfile().getId();
        if(!ListOfAvaiableNetworksForPlayer.containsKey(uuid)) {
            return null;
        }

        List<Integer> aviableNetworkIDSForPlayer = new ArrayList<>();

        for (Integer i : ListOfAvaiableNetworksForPlayer.get(uuid)) {
            aviableNetworkIDSForPlayer.add(i);
        }

        for (Integer i : mNetworks.keySet()) {
            aviableNetworkIDSForPlayer.remove(i);
        }

        if(aviableNetworkIDSForPlayer.isEmpty()) return null;

        Class<?> serverFluxNetworkClass = Class.forName("sonar.fluxnetworks.common.connection.ServerFluxNetwork");
        Constructor<?> constructor = serverFluxNetworkClass.getDeclaredConstructor(int.class, String.class, int.class, SecurityLevel.class, Player.class, String.class);
        constructor.setAccessible(true);
        ServerFluxNetwork network = (ServerFluxNetwork) constructor.newInstance(aviableNetworkIDSForPlayer.getFirst(), name, color, security, creator, password);

        if (mNetworks.put(network.getNetworkID(), network) != null) {
            FluxNetworks.LOGGER.warn("Network IDs are not unique when creating network, this shouldn't happen perhaps cleanup not working correctly?");
        }
        Channel.get().sendToAll(Messages.updateNetwork(network, (byte)21));
        return network;
    }

    @Overwrite(remap = false)
    private void read(@Nonnull CompoundTag compound){

    }

    @Overwrite(remap = false)
    public CompoundTag save(@Nonnull CompoundTag compound, HolderLookup.Provider registries) {
        return new CompoundTag();
    }


    @Override
    public void addStaticNetworkIDS(UUID pUUID, List<Integer> ids) {
        this.ListOfAvaiableNetworksForPlayer.put(pUUID, ids);
    }

    @Override
    public void readCustom(CompoundTag nbt) throws Exception {
        ListTag list = nbt.getList("networks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);

            Class<?> serverFluxNetworkClass = Class.forName("sonar.fluxnetworks.common.connection.ServerFluxNetwork");
            Constructor<?> constructor = serverFluxNetworkClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            ServerFluxNetwork network = (ServerFluxNetwork) constructor.newInstance();

            network.readCustomTag(tag, FluxConstants.NBT_SAVE_ALL);
            if (mNetworks.put(network.getNetworkID(), network) != null) {
                TBSync.getLOGGER().error("Network IDs are not unique when reading data (FLUX NETWORKS) if that happens there is a problem");
            }
        }
    }

    @Override
    public CompoundTag writeCustom(UUID pUUID) {
        ListTag list = new ListTag();

        for (FluxNetwork network : mNetworks.values()) {
            if(!network.getOwnerUUID().equals(pUUID)) continue;
            CompoundTag tag = new CompoundTag();
            network.writeCustomTag(tag, FluxConstants.NBT_SAVE_ALL);
            list.add(tag);
        }

        CompoundTag containsList = new CompoundTag();
        containsList.put("networks", list);
        return containsList;
    }

    @Override
    public Int2ObjectMap<FluxNetwork> getNetworks() {
        return this.mNetworks;
    }
}