package pl.techblock.sync.mixins.fluxnetworks;

import com.google.gson.internal.LinkedTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.*;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.logic.fluxnetworks.IFluxNetworksCustom;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.api.misc.FluxConstants;
import sonar.fluxnetworks.api.network.IFluxNetwork;
import sonar.fluxnetworks.api.network.SecurityType;
import sonar.fluxnetworks.common.connection.FluxNetworkServer;
import sonar.fluxnetworks.common.storage.FluxNetworkData;
import sonar.fluxnetworks.register.NetworkHandler;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(FluxNetworkData.class)
public abstract class FluxNetworksMixin extends WorldSavedData implements IFluxNetworksCustom {

    public FluxNetworksMixin(String p_i2141_1_) {
        super(p_i2141_1_);
    }


    @Shadow
    private final Int2ObjectMap<IFluxNetwork> networks = new Int2ObjectOpenHashMap<>();

    @Unique
    public Map<UUID, List<Integer>> ListOfAvaiableNetworksForPlayer = new LinkedTreeMap<>();



    @Nullable
    @Overwrite(remap = false)
    public IFluxNetwork createNetwork(@Nonnull PlayerEntity creator, String name, int color,
                                      SecurityType securityType, String password) {



        //only time it would not be loaded is when player would be on other server visiting island
        UUID uuid = creator.getGameProfile().getId();
        if(!ListOfAvaiableNetworksForPlayer.containsKey(uuid)) {
            return null;
        }

        List<Integer> aviableNetworkIDSForPlayer = new ArrayList<>();

        for (Integer i : ListOfAvaiableNetworksForPlayer.get(uuid)) {
            aviableNetworkIDSForPlayer.add(i);
        }

        for (Integer i : networks.keySet()) {
            aviableNetworkIDSForPlayer.remove(i);
        }

        if(aviableNetworkIDSForPlayer.isEmpty()) return null;

        FluxNetworkServer network = new FluxNetworkServer(aviableNetworkIDSForPlayer.get(0), name, color, creator);
        network.getSecurity().set(securityType, password);

        if (networks.put(network.getNetworkID(), network) != null) {
            FluxNetworks.LOGGER.warn("Network IDs are not unique when creating network");
        }
        NetworkHandler.sendToAll(NetworkHandler.S2C_UpdateNetwork(network, FluxConstants.TYPE_NET_BASIC));
        return network;
    }

    @Overwrite
    public void load(CompoundNBT compoundNBT) {

    }

    @Overwrite
    public CompoundNBT save(CompoundNBT compoundNBT) {
        return new CompoundNBT();
    }


    @Override
    public void addStaticNetworkIDS(UUID pUUID, List<Integer> ids) {
        this.ListOfAvaiableNetworksForPlayer.put(pUUID, ids);
    }

    @Override
    public void readCustom(CompoundNBT nbt) {
        ListNBT list = nbt.getList("networks", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT tag = list.getCompound(i);
            FluxNetworkServer network = new FluxNetworkServer();
            network.readCustomNBT(tag, FluxConstants.TYPE_SAVE_ALL);
            if (networks.put(network.getNetworkID(), network) != null) {
                TBSync.getLOGGER().error("Network IDs are not unique when reading data (FLUX NETWORKS) if that happens there is a problem");
            }
        }
    }

    @Override
    public CompoundNBT writeCustom(UUID pUUID) {
        ListNBT list = new ListNBT();

        for (IFluxNetwork network : networks.values()) {
            if(network.getOwnerUUID() == null) continue;
            if(!network.getOwnerUUID().equals(pUUID)) continue;
            CompoundNBT tag = new CompoundNBT();
            network.writeCustomNBT(tag, FluxConstants.TYPE_SAVE_ALL);
            list.add(tag);
        }

        CompoundNBT containsList = new CompoundNBT();
        containsList.put("networks", list);
        return containsList;
    }

    @Override
    public Int2ObjectMap<IFluxNetwork> getNetworks() {
        return this.networks;
    }
}
