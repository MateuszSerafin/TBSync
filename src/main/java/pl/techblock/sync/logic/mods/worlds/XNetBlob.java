package pl.techblock.sync.logic.mods.worlds;

import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import mcjty.xnet.multiblock.XNetWirelessChannels;
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
import pl.techblock.sync.logic.mods.duckinterfaces.IXnetBlobDataCustom;
import javax.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Blob;

public class XNetBlob implements IWorldSync {

    private String tableName = "XNet";

    public XNetBlob() {

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

    XNetWirelessChannels





    @Override
    public void saveWorldDataToDB(String worldName) {
        try {
            CompoundNBT toDB = new CompoundNBT();

            CompoundNBT blob = saveXnetBlobToNbt(worldName);
            if(blob != null){
                toDB.put("blob", blob);
            }


            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                CompressedStreamTools.writeCompressed(toDB, saveTo);
            }


            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsert(worldName, tableName, bis);
            bis.close();
        }
        catch (Exception e){
            TBSync.getLOGGER().error("Problem with XNet while saving data");
            e.printStackTrace();
        }
    }

    @Override
    public void loadWorldDataFromDB(String worldName) {
        try {
            Blob blob = DBManager.select(worldName, tableName);
            if(blob == null){
                return;
            }

            CompoundNBT tag = CompressedStreamTools.readCompressed(blob.getBinaryStream());
            loadXnetBlob(worldName, tag.getCompound("blob"));
            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with Xnet while loading data");
            e.printStackTrace();
        }
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
