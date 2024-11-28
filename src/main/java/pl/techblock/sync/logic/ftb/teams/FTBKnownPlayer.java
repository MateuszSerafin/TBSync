package pl.techblock.sync.logic.ftb.teams;

import dev.ftb.mods.ftbteams.data.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.logic.mods.duckinterfaces.IFTBTeamBaseCustom;
import pl.techblock.sync.logic.mods.duckinterfaces.IFTBTeamsCustom;
import java.io.*;
import java.sql.Blob;
import java.util.UUID;

public class FTBKnownPlayer implements IPlayerSync {

    private String tableName = "FTBTeamsKnownPlayers";

    private IFTBTeamsCustom giveInstance(){
        return (IFTBTeamsCustom) TeamManager.INSTANCE;
    }

    @Override
    public void saveToDB(UUID playerUUID) {
        //this is also called by FTBParty
        try {
            PlayerTeam team = giveInstance().knownPlayers().get(playerUUID);

            if(team == null){
                return;
            }

            CompoundNBT tag = team.serializeNBT();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (OutputStream saveTo = new BufferedOutputStream(bos)) {
                CompressedStreamTools.writeCompressed(tag, saveTo);
            }

            byte[] compressedData = bos.toByteArray();
            ByteArrayInputStream bis = new ByteArrayInputStream(compressedData);
            DBManager.upsert(playerUUID.toString(), tableName, bis);
            bis.close();
        }
        catch (Exception e){
            TBSync.getLOGGER().error("Problem with FTB Known Player while saving data");
            e.printStackTrace();
        }
    }

    @Override
    public void loadFromDB(UUID playerUUID) {
        try {
            //technically this can be loaded by FTBParty and reloading data will cause issues
            //it has no impact on cleanup or saving
            if(giveInstance().knownPlayers().containsKey(playerUUID)) return;


            Blob blob = DBManager.select(playerUUID.toString(), tableName);
            if(blob == null){
                //if player somehow has this null but FTB quests are not null in database it will die
                return;
            }
            PlayerTeam playerTeam = new PlayerTeam(TeamManager.INSTANCE);

            InputStream data = blob.getBinaryStream();
            CompoundNBT nbt = CompressedStreamTools.readCompressed(data);
            playerTeam.deserializeNBT(nbt);

            ((IFTBTeamBaseCustom) playerTeam).setUUID(playerUUID);
            giveInstance().teamMap().put(playerUUID, playerTeam);
            giveInstance().knownPlayers().put(playerUUID, playerTeam);

            data.close();
            blob.free();
        } catch (Exception e){
            TBSync.getLOGGER().error("Problem with FTB Known Player while loading data");
            e.printStackTrace();
        }
    }

    @Override
    public void cleanup(UUID playerUUID) {
        giveInstance().knownPlayers().remove(playerUUID);
        giveInstance().teamMap().remove(playerUUID);
    }
}
