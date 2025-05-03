package pl.techblock.sync.logic.xnet;

import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import pl.techblock.sync.TBSync;
import javax.annotation.Nullable;
import java.io.*;

public class XNetBlob {

    public XNetBlob() {}

    @Nullable
    public ByteArrayOutputStream savePerWorldModData(String worldName) throws Exception {
        CompoundTag nbt = saveXnetBlobToNbt(worldName);
        if(nbt == null) return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NbtIo.writeCompressed(nbt, out);
        return out;
    }

    public void loadPerWorldModData(String worldName, InputStream in) throws Exception {
        if(in == null) return;
        CompoundTag nbt = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
        loadXnetBlob(worldName, nbt);
    }

    @Nullable
    private CompoundTag saveXnetBlobToNbt(String worldName){
        ResourceKey<Level> worldRegistryKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(worldName));

        Level world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);
        if(world == null){
            TBSync.getLOGGER().error(String.format("Tried to save world %s but it doesn't exist", world));
            return null;
        }
        //no idea how to otherwise get instance of that
        XNetBlobData blobData = XNetBlobData.get(world);
        if(blobData == null) return null;
        WorldBlob blob = ((IXNetBlobDataCustom) blobData).getWorldBlobMap().get(worldRegistryKey);
        if(blob == null) return null;

        CompoundTag nbt = new CompoundTag();
        blob.writeToNBT(nbt);
        return nbt;
    }

    private void loadXnetBlob(String worldName, CompoundTag nbt){
        ResourceKey<Level> worldRegistryKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(worldName));

        Level world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);
        if(world == null){
            TBSync.getLOGGER().error(String.format("Tried to load world %s but it doesn't exist", world));
            return;
        }

        WorldBlob blob = new WorldBlob(worldRegistryKey);
        blob.readFromNBT(nbt);
        ((IXNetBlobDataCustom) XNetBlobData.get(world)).getWorldBlobMap().put(worldRegistryKey, blob);
        blob.recalculateNetwork();
    }

    public void cleanup(String worldName) {
        ResourceKey<Level> worldRegistryKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(worldName));


        Level world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);
        if(world == null){
            TBSync.getLOGGER().error(String.format("Tried to cleanup world %s but it doesn't exist", world));
            return;
        }
        XNetBlobData blobData = XNetBlobData.get(world);
        if(blobData == null) return;
        ((IXNetBlobDataCustom) blobData).getWorldBlobMap().remove(worldRegistryKey);
    }
}