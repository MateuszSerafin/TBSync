package pl.techblock.sync.logic.curios;

import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import pl.techblock.sync.TBSync;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import javax.annotation.Nullable;
import java.io.*;
import java.util.Optional;
import java.util.UUID;

//We use husksync to synchronize player inventories
//husksync does not touch curios
//this just exports curios which then will be entangled into husksync
//while curios provides an API that we can use the problem is we need to have access to Forge Data types inside plugin
//Surprisingly while the quality of that is not high it works and is the easiest way to do it
public class CuriosExport {

    @Nullable
    private static ServerPlayer getPlayerInstance(UUID playerUUID) throws Exception {
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if(player.getUUID().equals(playerUUID)) return player;
        }
        return null;
    }

    @Nullable
    public static ByteArrayOutputStream saveInventory(UUID playerUUID) throws Exception {
        ServerPlayer player = getPlayerInstance(playerUUID);

        if (player == null) {
            TBSync.getLogger().warn("Cannot save curios data - player {} is not online", playerUUID.toString());
            return null;
        }

        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);

        if(curiosInventory.isEmpty()) {
            TBSync.getLogger().info("No curios inventory found for player {}", playerUUID.toString());
            return null;
        }

        ListTag curiosTagData = curiosInventory.get().saveInventory(true);
        // we need to save CompoundTag, not ListTag
        CompoundTag tagOfList = new CompoundTag();
        tagOfList.put("list", curiosTagData);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream saveTo = new BufferedOutputStream(bos)) {
            NbtIo.writeCompressed(tagOfList, saveTo);
        }
        TBSync.getLogger().info("Successfully saved curios data for player-UUID {}", playerUUID.toString());
        return bos;
    }

    public static void loadInventory(UUID playerUUID, InputStream data) throws Exception {
        ServerPlayer player = getPlayerInstance(playerUUID);

        if (player == null) {
            TBSync.getLogger().warn("Cannot load curios data - player {} is not online", playerUUID.toString());
            return;
        }

        Optional<ICuriosItemHandler> curiosInventory = CuriosApi.getCuriosInventory(player);

        if(curiosInventory.isEmpty()) {
            TBSync.getLogger().info("No curios inventory found for player {}", playerUUID.toString());
            return;
        }

        CompoundTag tag = NbtIo.readCompressed(data, NbtAccounter.unlimitedHeap());
        data.close();
        //10 is id for compound nbt I thought it was for size or something
        ListTag originalCuriosData = tag.getList("list", 10);
        curiosInventory.get().loadInventory(originalCuriosData);
        TBSync.getLogger().info("Successfully loaded curios data for player-UUID {}", playerUUID.toString());
    }
}