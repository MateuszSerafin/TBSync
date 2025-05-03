package pl.techblock.sync.logic.enderstorage;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import codechicken.enderstorage.storage.EnderLiquidStorage;
import net.minecraft.nbt.*;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import pl.techblock.sync.TBSync;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderStorage {

    private IEnderStorageCustom getInstance(){
        return  (IEnderStorageCustom) EnderStorageManager.instance(false);
    }

    public EnderStorage() {}

    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        //string is something like white,white,white,type=fluid or smh,owner=uuid
        ListTag nbtListNBT = new ListTag();

        for (Map.Entry<String, AbstractEnderStorage> stringAbstractEnderStorageEntry : getInstance().getStorageMap().entrySet()) {

            AbstractEnderStorage val = stringAbstractEnderStorageEntry.getValue();
            Frequency freq = val.freq;
            if(freq.owner().isEmpty()) continue;
            if (!freq.owner().get().equals(playerUUID)) continue;

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
        return bos;
    }

    public void loadSaveData(UUID playerUUID, @Nullable InputStream in) throws Exception {
        if(in == null) return;
        CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
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
                    getInstance().getStorageMap().put(key, abstractEnderStorage);
                    getInstance().getStorageList().get(EnderLiquidStorage.TYPE).add(abstractEnderStorage);
                    break;
                case "item":
                    abstractEnderStorage = new EnderItemStorage(EnderStorageManager.instance(false), fromNbtFrequency);
                    abstractEnderStorage.loadFromTag(thisIsActualCompound.getCompound("abstractData"), ServerLifecycleHooks.getCurrentServer().registryAccess());
                    key = fromNbtFrequency + ",type=" + abstractEnderStorage.type();
                    getInstance().getStorageMap().put(key, abstractEnderStorage);
                    getInstance().getStorageList().get(EnderItemStorage.TYPE).add(abstractEnderStorage);
                    break;
                default:
                    TBSync.getLOGGER().error(String.format("Somehow found different type of storage for %s", playerUUID));
                    //let's not crash whole server just beacuse of it
                    continue;
            }
        }
        in.close();
    }

    //if we remove straight from map it will just create a new instance that has no items,liquids
    //ultra optimized code :5head:
    public void cleanup(UUID playerUUID) {
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

        for (String s : toDel) {
            getInstance().getStorageMap().remove(s);
        }

        for (AbstractEnderStorage abstractEnderStorage : toDelButStorage) {
            getInstance().getStorageList().get(EnderLiquidStorage.TYPE).remove(abstractEnderStorage);
            getInstance().getStorageList().get(EnderItemStorage.TYPE).remove(abstractEnderStorage);
        }
    }
}