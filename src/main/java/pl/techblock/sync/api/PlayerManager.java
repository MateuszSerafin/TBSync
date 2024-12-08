package pl.techblock.sync.api;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.enums.PlayerSync;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.logic.astral.AstralResearch;
import pl.techblock.sync.logic.cosmeticarmor.CosmeticArmor;
import pl.techblock.sync.logic.enderchests.EnderChests;
import pl.techblock.sync.logic.enderstorage.EnderStorage;
import pl.techblock.sync.logic.endertanks.EnderTanks;
import pl.techblock.sync.logic.fluxnetworks.FluxNetworks;
import pl.techblock.sync.logic.forgecapabilities.ForgeCaps;
import pl.techblock.sync.logic.futurepack.FuturePack;
import javax.annotation.Nullable;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PlayerManager {

    private static Map<PlayerSync, IPlayerSync> IPlayerSyncMap = new LinkedTreeMap<>();

    public static void init(){
        IPlayerSyncMap.put(PlayerSync.AstralResearch, new AstralResearch());
        IPlayerSyncMap.put(PlayerSync.FluxNetworks, new FluxNetworks());
        IPlayerSyncMap.put(PlayerSync.CosmeticArmor, new CosmeticArmor());
        IPlayerSyncMap.put(PlayerSync.FuturePack, new FuturePack());
        IPlayerSyncMap.put(PlayerSync.EnderStorage, new EnderStorage());
        IPlayerSyncMap.put(PlayerSync.EnderChests, new EnderChests());
        IPlayerSyncMap.put(PlayerSync.EnderTanks, new EnderTanks());
        IPlayerSyncMap.put(PlayerSync.ForgeCaps, new ForgeCaps());
    }

    public static void saveSpecificToDB(List<PlayerSync> whichOnes, UUID playerUUID){
        for (PlayerSync playerSync : whichOnes) {
            IPlayerSync IPlayerSync = IPlayerSyncMap.get(playerSync);

            try {
                IPlayerSync.saveToDB(playerUUID);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to save player data for %s but failed", playerSync.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void loadSpecificFromDB(List<PlayerSync> whichOnes, UUID playerUUID){
        for (PlayerSync playerSync : whichOnes) {
            IPlayerSync IPlayerSync = IPlayerSyncMap.get(playerSync);

            try {
                IPlayerSync.loadFromDB(playerUUID);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to load player data for %s but failed", playerSync.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void cleanUpSpecific(List<PlayerSync> whichOnes, UUID playerUUID){
        for (PlayerSync playerSync : whichOnes) {
            IPlayerSync IPlayerSync = IPlayerSyncMap.get(playerSync);

            try {
                IPlayerSync.cleanup(playerUUID);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to cleanup player data for %s but failed", playerSync.toString()));
                e.printStackTrace();
            }
        }
    }

    @Nullable
    public static IPlayerSync getSpecific(PlayerSync what){
        return IPlayerSyncMap.get(what);
    }

    public static void makeBackup(List<PlayerSync> whichOnes, UUID playerUUID) throws Exception{
        File folder = new File("./playerbackups/");
        if(!folder.exists()) folder.mkdir();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
        String formattedTime = now.format(formatter);
        File destination = new File(folder.toPath() + "/" + playerUUID.toString() + "." + formattedTime + ".bak");

        Map<PlayerSync, String> gsonThat = new HashMap<>();
        for (PlayerSync whichOne : whichOnes) {
            ByteArrayOutputStream data = IPlayerSyncMap.get(whichOne).getSaveData(playerUUID);
            if(data == null) continue;
            String stringData = Base64.getEncoder().encodeToString(data.toByteArray());
            data.close();
            gsonThat.put(whichOne, stringData);
        }
        if(gsonThat.isEmpty()) return;
        Gson gson = new Gson();
        FileWriter writer = new FileWriter(destination);
        gson.toJson(gsonThat, writer);
        writer.flush();
        writer.close();
    }

    public static void loadBackup(File source, List<PlayerSync> whichOnes, UUID playerUUID) throws Exception {
        if(!source.exists()){
            TBSync.getLOGGER().error(String.format("Source backup file does not exist for player Backup %s", playerUUID.toString()));
            return;
        }
        Gson gson = new Gson();
        HashMap<PlayerSync, String> rawData = gson.fromJson(new FileReader(source), new TypeToken<HashMap<PlayerSync, String>>() {}.getType());

        for (PlayerSync whichOne : whichOnes) {
            String requiresDecoding = rawData.get(whichOne);
            if(requiresDecoding == null){
                IPlayerSyncMap.get(whichOne).loadSaveData(playerUUID, null);
                continue;
            }
            byte[] rawBytes = Base64.getDecoder().decode(requiresDecoding);
            IPlayerSyncMap.get(whichOne).loadSaveData(playerUUID, new ByteArrayInputStream(rawBytes));
        }
    }
}