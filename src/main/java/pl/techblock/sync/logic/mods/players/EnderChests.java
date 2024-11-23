package pl.techblock.sync.logic.mods.players;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.logic.interfaces.IPlayerSync;
import pl.techblock.sync.mixins.EnderChestsAccess;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.util.Map;
import java.util.UUID;

public class EnderChests implements IPlayerSync {
    //2137 mixins or reflection
    private String tableName = "EnderChests";

    public EnderChests() {

    }

    @Override
    public void saveToDB(UUID playerUUID) {
        //crime crime crime
        Map dataForPlayer = EnderChestsAccess.getDatabase().row(playerUUID.toString());
        if(dataForPlayer.isEmpty()) return;

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


            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsert(playerUUID.toString(), tableName, bis);
            bis.close();
        }
        catch (Exception e){
            TBSync.getLOGGER().error("Problem with EnderChests while saving data to database");
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        CompoundNBT tag = null;
        try {
            Blob blob = DBManager.select(playerUUID.toString(), tableName);
            if(blob == null){
                return;
            }
            tag = CompressedStreamTools.readCompressed(blob.getBinaryStream());
            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with EnderChests while loading data from database");
            e.printStackTrace();
        }

        if(tag == null) return;

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
    }

    @Override
    public void cleanup(UUID playerUUID) {
        EnderChestsAccess.getDatabase().row(playerUUID.toString()).clear();
    }
}
