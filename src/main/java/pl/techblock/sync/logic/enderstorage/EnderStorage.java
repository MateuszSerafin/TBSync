package pl.techblock.sync.logic.enderstorage;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import codechicken.enderstorage.storage.EnderLiquidStorage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import pl.techblock.sync.TBSync;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderStorage {

    private IEnderStorageCustom getInstance(){
        return  (IEnderStorageCustom) EnderStorageManager.instance(false);
    }

    public EnderStorage() {}

    @Nullable
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        //string is something like white,white,white,type=fluid or smh,owner=uuid
        ListNBT nbtListNBT = new ListNBT();

        for (Map.Entry<String, AbstractEnderStorage> stringAbstractEnderStorageEntry : getInstance().getStorageMap().entrySet()) {
            AbstractEnderStorage val = stringAbstractEnderStorageEntry.getValue();
            Frequency freq = val.freq;
            if(freq.getOwner() == null) continue;
            if (!freq.getOwner().equals(playerUUID)) continue;

            CompoundNBT saveToThat = new CompoundNBT();
            freq.writeToNBT(saveToThat);
            saveToThat.put("abstractData", val.saveToTag());
            //it doesn't actually save type of it it needs to be saved on it's own and then instance needs to be created like that
            //item or liquid
            saveToThat.putString("abstractType", val.type());
            nbtListNBT.add(saveToThat);
        }
        //pointless to call db with just empty tag
        if (nbtListNBT.isEmpty()) return null;

        CompoundNBT tagOfList = new CompoundNBT();
        tagOfList.put("list", nbtListNBT);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (OutputStream saveTo = new BufferedOutputStream(bos)) {
            CompressedStreamTools.writeCompressed(tagOfList, saveTo);
        }
        return bos;
    }

    public void loadSaveData(UUID playerUUID, InputStream in) throws Exception {
        if(in == null) return;
        CompoundNBT tag = CompressedStreamTools.readCompressed(in);
        //10 is id for compound nbt i thought it was for size or something
        for (INBT list : tag.getList("list", 10)) {
            CompoundNBT thisIsActualCompound = (CompoundNBT) list;
            Frequency fromNbtFrequency = new Frequency(thisIsActualCompound);
            String type = thisIsActualCompound.getString("abstractType");

            AbstractEnderStorage abstractEnderStorage;
            String key;

            switch (type){
                case "liquid":
                    abstractEnderStorage = new EnderLiquidStorage(EnderStorageManager.instance(false), fromNbtFrequency);
                    abstractEnderStorage.loadFromTag(thisIsActualCompound.getCompound("abstractData"));
                    key = fromNbtFrequency + ",type=" + abstractEnderStorage.type();
                    getInstance().getStorageMap().put(key, abstractEnderStorage);
                    getInstance().getStorageList().get(EnderLiquidStorage.TYPE).add(abstractEnderStorage);
                    break;
                case "item":
                    abstractEnderStorage = new EnderItemStorage(EnderStorageManager.instance(false), fromNbtFrequency);
                    abstractEnderStorage.loadFromTag(thisIsActualCompound.getCompound("abstractData"));
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
            if(storage.freq.getOwner() != null){
                if(storage.freq.getOwner().equals(playerUUID)){
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