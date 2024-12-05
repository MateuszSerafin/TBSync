package pl.techblock.sync.logic.endertanks;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.mixins.endertanks.EnderTanksAccess;
import shetiphian.endertanks.common.misc.EnderContainer;
import javax.annotation.Nullable;
import java.io.*;
import java.sql.Blob;
import java.util.Map;
import java.util.UUID;

//i have no idea why for chests the Container class was not public and there is
public class EnderTanks implements IPlayerSync {

    private String tableName = "EnderTanks";

    public EnderTanks(){
        DBManager.createTable(tableName);
    }

    @Nullable
    @Override
    public ByteArrayOutputStream getSaveData(UUID playerUUID) throws Exception {
        Map<String, EnderContainer> dataForPlayer = EnderTanksAccess.getDatabase().row(playerUUID.toString());
        if(dataForPlayer.isEmpty()) return null;

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
        return bos;
    }

    @Override
    public void saveToDB(UUID playerUUID) {
        try {
            ByteArrayOutputStream bos = getSaveData(playerUUID);
            if(bos == null) return;
            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsertBlob(playerUUID.toString(), tableName, bis);
            bis.close();
        }
        catch (Exception e){
            TBSync.getLOGGER().error(String.format("Problem with EnderTanks while saving data to database %s", playerUUID));
            e.printStackTrace();
        }
    }

    @Override
    public void loadSaveData(UUID playerUUID, InputStream in) throws Exception {
        if(in == null) return;
        CompoundNBT tag = CompressedStreamTools.readCompressed(in);

        for (String code : tag.getAllKeys()) {
            EnderContainer container = new EnderContainer(playerUUID.toString(), code);
            container.load(tag.getCompound(code));
            EnderTanksAccess.getDatabase().put(playerUUID.toString(), code, container);
        }
        in.close();
    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        try {
            Blob blob = DBManager.selectBlob(playerUUID.toString(), tableName);
            if(blob == null){
                return;
            }
            loadSaveData(playerUUID, blob.getBinaryStream());
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
