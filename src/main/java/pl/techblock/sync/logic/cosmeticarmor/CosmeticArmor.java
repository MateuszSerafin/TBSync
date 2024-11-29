package pl.techblock.sync.logic.cosmeticarmor;

import lain.mods.cos.impl.ModObjects;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.util.UUID;

public class CosmeticArmor implements IPlayerSync {

    private String tableName = "CosmeticArmor";

    private ICosmeticArmorCustom instance;

    public CosmeticArmor(){
        instance = (ICosmeticArmorCustom) ModObjects.invMan;
        DBManager.createTable(tableName);
    }

    @Override
    public void saveToDB(UUID playerUUID) {
        try {
            CompoundNBT tag = instance.writeCustom(playerUUID);

            if(tag == null){
                TBSync.getLOGGER().error("This is edge case theoretically it should be there, something with not initializing cache for cosmetic armor");
                return;
            }

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
            TBSync.getLOGGER().error("Problem with Cosmetic Armor while saving data");
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        try {
            Blob blob = DBManager.selectBlob(playerUUID.toString(), tableName);
            if(blob == null){
                //yes it handles null, it will create empty instance, this is how author did it
                instance.readCustom(playerUUID, null);
                return;
            }

            CompoundNBT tag = CompressedStreamTools.readCompressed(blob.getBinaryStream());
            instance.readCustom(playerUUID, tag);

            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with Cosmetic Armor while loading data");
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(UUID playerUUID) {
        instance.invalidate(playerUUID);
    }
}
