package pl.techblock.sync.logic.mods.players;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.logic.interfaces.IPlayerSync;
import pl.techblock.sync.mixins.EnderTanksAccess;
import shetiphian.endertanks.common.misc.EnderContainer;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.util.Map;
import java.util.UUID;

//i have no idea why for chests the Container class was not public and there is
public class EnderTanks implements IPlayerSync {

    private String tableName = "EnderTanks";

    public EnderTanks(){

    }

    @Override
    public void saveToDB(UUID playerUUID) {
        try {
        Map<String, EnderContainer> dataForPlayer = EnderTanksAccess.getDatabase().row(playerUUID.toString());
        if(dataForPlayer.isEmpty()) return;

        CompoundNBT fileNBT = new CompoundNBT();

        for (Map.Entry<String, EnderContainer> stringEnderContainerEntry : dataForPlayer.entrySet()) {
            String code = stringEnderContainerEntry.getKey();
            EnderContainer enderContainer = stringEnderContainerEntry.getValue();
            fileNBT.put(code, enderContainer.save());
        }

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
            TBSync.getLOGGER().error(String.format("Problem with EnderTanks while saving data to database %s", playerUUID));
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        try {
            Blob blob = DBManager.select(playerUUID.toString(), tableName);
            if(blob == null){
                return;
            }
            CompoundNBT tag = CompressedStreamTools.readCompressed(blob.getBinaryStream());

            for (String code : tag.getAllKeys()) {
                EnderContainer container = new EnderContainer(playerUUID.toString(), code);
                container.load(tag.getCompound(code));
                EnderTanksAccess.getDatabase().put(playerUUID.toString(), code, container);
            }
            blob.free();

        } catch (Exception e){
            TBSync.getLOGGER().error(String.format("Problem with EnderTanks while loading data from database %s", playerUUID));
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(UUID playerUUID) {
        EnderTanksAccess.getDatabase().row(playerUUID.toString()).clear();
    }
}
