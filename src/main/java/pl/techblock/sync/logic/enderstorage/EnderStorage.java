package pl.techblock.sync.logic.enderstorage;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import codechicken.enderstorage.storage.EnderLiquidStorage;
import net.minecraft.nbt.*;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.IPlayerSync;
import pl.techblock.sync.db.DBManager;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderStorage implements IPlayerSync {

    private final String enderStorageTable = "EnderStorage";

    public EnderStorage() {
        DBManager.createTable(enderStorageTable);
    }

    @Override
    public void savePlayerToDB(UUID playerUUID) {
        ByteArrayOutputStream data = savePlayer(playerUUID);
        DBManager.upsertBlob(playerUUID.toString(), enderStorageTable, new ByteArrayInputStream(data.toByteArray()));
    }

    @Override
    public void loadPlayerFromDB(UUID playerUUID) {
        Blob data = DBManager.selectBlob(playerUUID.toString(), enderStorageTable);
        if(data == null){
            loadPlayer(playerUUID, null);
            return;
        }
        try {
            loadPlayer(playerUUID, data.getBinaryStream());
        } catch (Exception e){
            TBSync.getLogger().error("Caught error while loading data from database for enderstorage {}", playerUUID.toString());
            TBSync.getLogger().error(e.getMessage());
        }
    }

    @Override
    public void playerCleanUp(UUID playerUUID) {
        List<String> toDel = new ArrayList<>();
        List<AbstractEnderStorage> toDelButStorage = new ArrayList<>();

        for (Map.Entry<String, AbstractEnderStorage> stringAbstractEnderStorageEntry : getInstance().getStorageMap().entrySet()) {
            AbstractEnderStorage storage = stringAbstractEnderStorageEntry.getValue();
            if(storage.freq.owner().isPresent()){
                if(storage.freq.owner().get().equals(playerUUID)){
                    toDel.add(stringAbstractEnderStorageEntry.getKey());
                    toDelButStorage.add(storage);
                }
            }
        }

        TBSync.getLogger().info("Deleting {} storages for Player-UUID {}", toDel.size(), playerUUID.toString());

        for (String s : toDel) {
            getInstance().getStorageMap().remove(s);
        }

        for (AbstractEnderStorage abstractEnderStorage : toDelButStorage) {
            getInstance().getStorageList().get(EnderLiquidStorage.TYPE).remove(abstractEnderStorage);
            getInstance().getStorageList().get(EnderItemStorage.TYPE).remove(abstractEnderStorage);
        }
    }

    @Nullable
    @Override
    public ByteArrayOutputStream savePlayer(UUID playerUUID) {
        try {
            //string is something like white,white,white,type=fluid or smh,owner=uuid
            ListTag nbtListNBT = new ListTag();

            for (Map.Entry<String, AbstractEnderStorage> stringAbstractEnderStorageEntry : getInstance().getStorageMap().entrySet()) {

                AbstractEnderStorage val = stringAbstractEnderStorageEntry.getValue();
                Frequency freq = val.freq;
                if (freq.owner().isEmpty()) continue;
                if (!freq.owner().get().equals(playerUUID)) continue;
                TBSync.getLogger().info("Found storage ({}) for player {}", freq.toString(), playerUUID.toString());
                Class<?> frequencyClass = Class.forName("codechicken.enderstorage.api.Frequency");
                Method saveToNbtMethod = frequencyClass.getDeclaredMethod("write_internal", CompoundTag.class);
                saveToNbtMethod.setAccessible(true);
                CompoundTag base = (CompoundTag) saveToNbtMethod.invoke(freq, new CompoundTag());
                base.putString("abstractType", val.type());
                base.put("abstractData", stringAbstractEnderStorageEntry.getValue().saveToTag(ServerLifecycleHooks.getCurrentServer().registryAccess()));

                nbtListNBT.add(base);
            }
            // we need to save CompoundTag, not ListTag
            CompoundTag tagOfList = new CompoundTag();
            tagOfList.put("list", nbtListNBT);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                NbtIo.writeCompressed(tagOfList, saveTo);
            }
            TBSync.getLogger().info("Successfully gathered storages for player {}", playerUUID.toString());
            return bos;
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught gathering data for enderstorage, Player-UUID: {}", playerUUID.toString());
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }

    @Override
    public void loadPlayer(UUID playerUUID, @Nullable InputStream data) {
        if(data == null) return;
        try {
            CompoundTag tag = NbtIo.readCompressed(data, NbtAccounter.unlimitedHeap());
            //10 is id for compound nbt I thought it was for size or something
            for (Tag list : tag.getList("list", 10)) {
                CompoundTag thisIsActualCompound = (CompoundTag) list;
                Frequency fromNbtFrequency = new Frequency(thisIsActualCompound);
                String type = thisIsActualCompound.getString("abstractType");

                AbstractEnderStorage abstractEnderStorage;
                String key;

                switch (type){
                    case "liquid":
                        abstractEnderStorage = new EnderLiquidStorage(EnderStorageManager.instance(false), fromNbtFrequency);
                        abstractEnderStorage.loadFromTag(thisIsActualCompound.getCompound("abstractData"), ServerLifecycleHooks.getCurrentServer().registryAccess());
                        key = fromNbtFrequency + ",type=" + abstractEnderStorage.type();
                        TBSync.getLogger().info("Loading storage {} for Player-UUID {}", key, playerUUID.toString());
                        getInstance().getStorageMap().put(key, abstractEnderStorage);
                        getInstance().getStorageList().get(EnderLiquidStorage.TYPE).add(abstractEnderStorage);
                        break;
                    case "item":
                        abstractEnderStorage = new EnderItemStorage(EnderStorageManager.instance(false), fromNbtFrequency);
                        abstractEnderStorage.loadFromTag(thisIsActualCompound.getCompound("abstractData"), ServerLifecycleHooks.getCurrentServer().registryAccess());
                        key = fromNbtFrequency + ",type=" + abstractEnderStorage.type();
                        TBSync.getLogger().info("Loading storage {} for Player-UUID {}", key, playerUUID.toString());
                        getInstance().getStorageMap().put(key, abstractEnderStorage);
                        getInstance().getStorageList().get(EnderItemStorage.TYPE).add(abstractEnderStorage);
                        break;
                    default:
                        TBSync.getLogger().error("Somehow found different type of storage for {}", playerUUID.toString());
                        //let's not crash whole server just because of it
                        continue;
                }
            }
            data.close();
        } catch (Exception e){
            TBSync.getLogger().error("A problem was caught loading data for enderstorage, Player-UUID: {}", playerUUID);
            TBSync.getLogger().error(e.getMessage());
            throw new RuntimeException("This shouldn't happen, let's crash");
        }
    }

    private IEnderStorageCustom getInstance(){
        return (IEnderStorageCustom) EnderStorageManager.instance(false);
    }
}