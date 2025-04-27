package pl.techblock.sync.logic.enderchests;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.mixins.enderchests.EnderChestsAccess;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public class EnderChests {

    public EnderChests() {}

    @Nullable
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        //crime crime crime
        Map dataForPlayer = EnderChestsAccess.getDatabase().row(playerUUID.toString());
        if(dataForPlayer.isEmpty()) return null;

        CompoundNBT fileNBT = new CompoundNBT();

        for (Object o : dataForPlayer.entrySet()) {
            String code = ((Map.Entry<String, Object>) o).getKey();
            Object privateClassInstance = ((Map.Entry<String, Object>) o).getValue();

            CompoundNBT nbtPocket = null;
            try {
                Class<?> chestDataClass = Class.forName("shetiphian.enderchests.common.misc.ChestData");
                Method saveToNbtMethod = chestDataClass.getDeclaredMethod("saveToNBT", CompoundNBT.class);
                saveToNbtMethod.setAccessible(true);
                nbtPocket = (CompoundNBT) saveToNbtMethod.invoke(privateClassInstance, new CompoundNBT());
                fileNBT.put(code, nbtPocket);
            } catch (Exception e) {
                TBSync.getLOGGER().error(String.format("Something died while trying to save ender chests data for %s (reflection)", playerUUID));
                e.printStackTrace();
                continue;
            }
        }

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                CompressedStreamTools.writeCompressed(fileNBT, saveTo);
            }
            return bos;
        } catch (Exception e){
            return null;
        }
    }

    public void loadSaveData(UUID playerUUID, InputStream in) throws Exception {
        if(in == null) return;
        CompoundNBT tag = CompressedStreamTools.readCompressed(in);
        for (String code : tag.getAllKeys()) {
            try {
                Class<?> chestDataClass = Class.forName("shetiphian.enderchests.common.misc.ChestData");
                Constructor<?> constructor = chestDataClass.getDeclaredConstructor(String.class, String.class);
                constructor.setAccessible(true);
                Object chestDataPrivateInstance = constructor.newInstance(playerUUID.toString(), code);


                Method loadFromNBT = chestDataClass.getDeclaredMethod("loadFromNBT", CompoundNBT.class);
                loadFromNBT.setAccessible(true);
                loadFromNBT.invoke(chestDataPrivateInstance, tag.getCompound(code));
                EnderChestsAccess.getDatabase().put(playerUUID.toString(), code, chestDataPrivateInstance);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Reflection dying for Ender chests %s", playerUUID));
                e.printStackTrace();
                continue;
            }
        }
        in.close();
    }

    public void cleanup(UUID playerUUID) {
        EnderChestsAccess.getDatabase().row(playerUUID.toString()).clear();
    }
}
