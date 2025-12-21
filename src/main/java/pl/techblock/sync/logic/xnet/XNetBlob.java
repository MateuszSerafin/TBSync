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
import pl.techblock.sync.api.IWorldSync;
import pl.techblock.sync.db.DBManager;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.Blob;

public class XNetBlob implements IWorldSync {

    private final String tableName = "XNet";

    public XNetBlob() {
        DBManager.createTable(tableName);
    }

    @Override
    public void savePerWorldModDataToDB(String worldName) {
        ByteArrayOutputStream data = saveWorld(worldName);
        if(data == null) return;
        DBManager.upsertBlob(worldName, tableName, new ByteArrayInputStream(data.toByteArray()));
    }

    @Override
    public void loadPerWorldModDataFromDB(String worldName) {
        try {
            Blob data = DBManager.selectBlob(worldName, tableName);
            if (data == null) {
                loadWorld(worldName, null);
                return;
            }
            loadWorld(worldName, data.getBinaryStream());
        } catch (Exception e){
            TBSync.getLogger().error("Caught error while loading data from database for XNet WorldName: {}", worldName);
            TBSync.getLogger().error(e.getMessage());
        }
    }

    @Override
    public void worldCleanUp(String worldName) {
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(worldName));

        Level world = ServerLifecycleHooks.getCurrentServer().getLevel(worldKey);
        if(world == null){
            TBSync.getLogger().error(String.format("Tried to cleanup world %s but it doesn't exist", worldName));
            return;
        }
        XNetBlobData blobData = XNetBlobData.get(world);
        if(blobData == null) return;
        ((IXNetBlobDataCustom) blobData).getWorldBlobMap().remove(worldKey);
    }

    @Override
    public @org.jetbrains.annotations.Nullable ByteArrayOutputStream saveWorld(String worldName) {
        try {
            CompoundTag nbtData = saveXnetBlobToNbt(worldName);
            if(nbtData == null) return null;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NbtIo.writeCompressed(nbtData, output);
            return output;
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught gathering data for XNet, WorldName: {}", worldName);
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }

    @Override
    public void loadWorld(String worldName, @org.jetbrains.annotations.Nullable InputStream data) {
        try {
            if(data == null) return;
            CompoundTag nbtData = NbtIo.readCompressed(data, NbtAccounter.unlimitedHeap());
            loadXnetBlob(worldName, nbtData);
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught loading data for XNet, WorldName: {}", worldName);
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }

    @Nullable
    private CompoundTag saveXnetBlobToNbt(String worldName){
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(worldName));

        Level world = ServerLifecycleHooks.getCurrentServer().getLevel(worldKey);
        if(world == null){
            TBSync.getLogger().error(String.format("Tried to save world %s but it doesn't exist", worldName));
            return null;
        }
        //no idea how to otherwise get instance of that
        XNetBlobData blobData = XNetBlobData.get(world);
        if(blobData == null) return null;
        WorldBlob blob = ((IXNetBlobDataCustom) blobData).getWorldBlobMap().get(worldKey);
        if(blob == null) return null;

        CompoundTag nbt = new CompoundTag();
        blob.writeToNBT(nbt);
        return nbt;
    }

    private void loadXnetBlob(String worldName, CompoundTag nbt){
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.tryParse(worldName));

        Level world = ServerLifecycleHooks.getCurrentServer().getLevel(worldKey);
        if(world == null){
            TBSync.getLogger().error(String.format("Tried to load world %s but it doesn't exist", world));
            return;
        }

        WorldBlob blob = new WorldBlob(worldKey);
        blob.readFromNBT(nbt);
        ((IXNetBlobDataCustom) XNetBlobData.get(world)).getWorldBlobMap().put(worldKey, blob);
        blob.recalculateNetwork();
    }
}