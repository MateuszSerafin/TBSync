package pl.techblock.sync.logic.xnet;

import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.xnet.XNet;
import mcjty.xnet.multiblock.WirelessChannelKey;
import mcjty.xnet.multiblock.XNetWirelessChannels;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.db.DBManager;
import javax.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//it looks like i fcked up while testing and wireless bit did not require synchronization i am leaving that for future use just in case
//(if it saves data to the world directory i assumed it's important but it looks like channels were working normally by just loading world no idea)
@Deprecated
public class XNetWireless implements IPlayerSync {

    private String tableName = "XNetWireless";

    public XNetWireless() {
        DBManager.createTable(tableName);
    }

    //yea it will cause issues if overworld is not loaded or whatever (but either way mod would not save data and cause issues as its main folder for saves
    //i don't even know if you can not load overworld
    private IXnetWirelessCustom getInstance(){
        RegistryKey<World> worldRegistryKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("minecraft:overworld"));
        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);
        if (world == null) {
            TBSync.getLOGGER().error(String.format("Tried to save world %s but it doesn't exist", world));
            return null;
        }
        return ((IXnetWirelessCustom) XNetWirelessChannels.get(world));
    }

    @Nullable
    private CompoundNBT getCompound(UUID playerUUID) {
        Map<WirelessChannelKey, XNetWirelessChannels.WirelessChannelInfo> map = getInstance().getChannelToWireless();

        CompoundNBT toReturn = new CompoundNBT();

        ListNBT channelTagList = new ListNBT();

        for (Map.Entry<WirelessChannelKey, XNetWirelessChannels.WirelessChannelInfo> wirelessChannelKeyWirelessChannelInfoEntry : map.entrySet()) {
            WirelessChannelKey key = wirelessChannelKeyWirelessChannelInfoEntry.getKey();
            XNetWirelessChannels.WirelessChannelInfo channelInfo = wirelessChannelKeyWirelessChannelInfoEntry.getValue();

            if(!key.getOwner().equals(playerUUID)){
                continue;
            }

            CompoundNBT channelTc = new CompoundNBT();
            channelTc.putString("name", key.getName());
            channelTc.putString("type", key.getChannelType().getID());
            if (key.getOwner() != null) {
                channelTc.putUUID("owner", key.getOwner());
            }

            channelTc.put("routers", getInstance().exposedWriteRouters(channelInfo));
            channelTagList.add(channelTc);
        }
        toReturn.put("channels", channelTagList);
        return toReturn;
    }

    @Override
    public void saveToDB(UUID playerUUID) throws Exception {
        try {
            CompoundNBT tag = getCompound(playerUUID);
            if(tag == null) return;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                CompressedStreamTools.writeCompressed(tag, saveTo);
            }

            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsertBlob(playerUUID.toString(), tableName, bis);
            bis.close();
        }
        catch (Exception e){
            TBSync.getLOGGER().error("Problem with XNetWireless while saving data");
            e.printStackTrace();
        }
    }

    private void loadNbt(CompoundNBT compound){
        ListNBT tagList = compound.getList("channels", 10);

        for(int i = 0; i < tagList.size(); ++i) {
            CompoundNBT tc = tagList.getCompound(i);
            XNetWirelessChannels.WirelessChannelInfo channelInfo = new XNetWirelessChannels.WirelessChannelInfo();
            getInstance().exposedReadRouters(tc.getList("routers", 10), channelInfo);
            UUID owner = null;
            if (tc.contains("owner")) {
                owner = tc.getUUID("owner");
            }

            String name = tc.getString("name");
            IChannelType type = XNet.xNetApi.findType(tc.getString("type"));
            getInstance().getChannelToWireless().put(new WirelessChannelKey(name, type, owner), channelInfo);
        }
    }

    @Override
    public void loadFromDB(UUID playerUUID) throws Exception {
        try {
            Blob blob = DBManager.selectBlob(playerUUID.toString(), tableName);
            if(blob == null){
                //it can happen, if it's not in db and load is called, like when first time creating an island and it tries to load nothing
                return;
            }
            CompoundNBT tag = CompressedStreamTools.readCompressed(blob.getBinaryStream());
            loadNbt(tag);
            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with XNetWireless while loading data");
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(UUID playerUUID) throws Exception {
        RegistryKey<World> worldRegistryKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("minecraft:overworld"));

        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);
        if (world == null) {
            TBSync.getLOGGER().error(String.format("Tried to cleanup world %s but it doesn't exist", world));
            return;
        }
        List<WirelessChannelKey> toDelete = new ArrayList<>();
        for (Map.Entry<WirelessChannelKey, XNetWirelessChannels.WirelessChannelInfo> wirelessChannelKeyWirelessChannelInfoEntry : ((IXnetWirelessCustom) XNetWirelessChannels.get(world)).getChannelToWireless().entrySet()) {
            WirelessChannelKey key = wirelessChannelKeyWirelessChannelInfoEntry.getKey();
            if(playerUUID.equals(key.getOwner())){
                toDelete.add(key);
            }
        }

        for (WirelessChannelKey wirelessChannelKey : toDelete) {
            ((IXnetWirelessCustom) XNetWirelessChannels.get(world)).getChannelToWireless().remove(wirelessChannelKey);
        }
    }
}

