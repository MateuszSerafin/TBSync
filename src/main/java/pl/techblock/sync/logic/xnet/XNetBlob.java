package pl.techblock.sync.logic.xnet;

import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.interfaces.IWorldSync;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.Blob;

public class XNetBlob implements IWorldSync {

    private String tableName = "XNet";

    public XNetBlob() {
        DBManager.createTable(tableName);
    }

    @Nullable
    @Override
    public ByteArrayOutputStream savePerWorldModData(String worldName) throws Exception {
        CompoundNBT nbt = saveXnetBlobToNbt(worldName);
        if(nbt == null) return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(nbt, out);
        return out;
    }

    @Override
    public void savePerWorldModDataToDB(String worldName) throws Exception {
        try {
            ByteArrayOutputStream bos = savePerWorldModData(worldName);
            if(bos == null) return;
            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsertBlob(worldName, tableName, bis);
            bis.close();
        }
        catch (Exception e){
            TBSync.getLOGGER().error("Problem with XNet while saving data");
            e.printStackTrace();
        }
    }

    @Override
    public void loadPerWorldModData(String worldName, InputStream in) throws Exception {
        if(in == null) return;
        CompoundNBT nbt = CompressedStreamTools.readCompressed(in);
        loadXnetBlob(worldName, nbt);
    }

    @Override
    public void loadPerWorldModDataFromDB(String worldName) throws Exception {
        try {
            Blob blob = DBManager.selectBlob(worldName, tableName);
            if(blob == null){
                return;
            }
            loadPerWorldModData(worldName, blob.getBinaryStream());
            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with Xnet while loading data");
            e.printStackTrace();
        }
    }

    @Nullable
    private CompoundNBT saveXnetBlobToNbt(String worldName){
        RegistryKey<World> worldRegistryKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(worldName));

        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);
        if(world == null){
            TBSync.getLOGGER().error(String.format("Tried to save world %s but it doesn't exist", world));
            return null;
        }
        //no idea how to otherwise get instance of that
        XNetBlobData blobData = XNetBlobData.get(world);
        if(blobData == null) return null;
        WorldBlob blob = ((IXnetBlobDataCustom) blobData).getWorldBlobMap().get(worldRegistryKey);

        if(blob == null) return null;

        CompoundNBT nbt = new CompoundNBT();
        blob.writeToNBT(nbt);
        return nbt;
    }

    private void loadXnetBlob(String worldName, CompoundNBT nbt){
        RegistryKey<World> worldRegistryKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(worldName));

        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);
        if(world == null){
            TBSync.getLOGGER().error(String.format("Tried to load world %s but it doesn't exist", world));
            return;
        }

        WorldBlob blob = new WorldBlob(worldRegistryKey);
        blob.readFromNBT(nbt);
        ((IXnetBlobDataCustom) XNetBlobData.get(world)).getWorldBlobMap().put(worldRegistryKey, blob);
        blob.recalculateNetwork();
    }
    @Override
    public void cleanup(String worldName) {
        RegistryKey<World> worldRegistryKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(worldName));

        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);
        if(world == null){
            TBSync.getLOGGER().error(String.format("Tried to cleanup world %s but it doesn't exist", world));
            return;
        }
        XNetBlobData blobData = XNetBlobData.get(world);
        if(blobData == null) return;
        ((IXnetBlobDataCustom) blobData).getWorldBlobMap().remove(worldRegistryKey);
    }
}
