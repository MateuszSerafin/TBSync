package pl.techblock.sync.api;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.enums.WorldSync;
import pl.techblock.sync.api.interfaces.IWorldSync;
import pl.techblock.sync.logic.xnet.XNetBlob;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

//there is assumption that world names do not change, so if you change server
//make sure that world name is exactly the same (it will not work otherwise)
//we use uuid's for world names (as strings)
public class WorldManager {

    private static Map<WorldSync, IWorldSync> IWorldSyncMap = new LinkedTreeMap<>();

    public static void init(){
        IWorldSyncMap.put(WorldSync.XNetBlobData, new XNetBlob());
    }

    public static void saveSpecificToDB(List<WorldSync> whichOnes, String worldName){
        for (WorldSync worldSync : whichOnes) {
            IWorldSync IWorldSync = IWorldSyncMap.get(worldSync);
            try {
                IWorldSync.savePerWorldModDataToDB(worldName);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to save world data for %s but failed", worldSync.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void loadSpecificFromDB(List<WorldSync> whichOnes, String worldName){
        for (WorldSync worldSync : whichOnes) {
            IWorldSync IWorldSync = IWorldSyncMap.get(worldSync);
            try {
                IWorldSync.loadPerWorldModDataFromDB(worldName);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to load world data for %s but failed", worldSync.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void cleanUpSpecific(List<WorldSync> whichOnes, String worldName){
        for (WorldSync worldSync : whichOnes) {
            IWorldSync IWorldSync = IWorldSyncMap.get(worldSync);
            try {
                IWorldSync.cleanup(worldName);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to cleanup world data for %s but failed", worldSync.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void makeBackup(List<WorldSync> whichOnes, String worldName) throws Exception{
        File folder = new File("./worldbackups/");
        if(!folder.exists()) folder.mkdir();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
        String formattedTime = now.format(formatter);
        File destination = new File(folder.toPath() + "/" + worldName + "." + formattedTime + ".bak");

        Map<WorldSync, String> gsonThat = new HashMap<>();
        for (WorldSync whichOne : whichOnes) {
            ByteArrayOutputStream data = IWorldSyncMap.get(whichOne).savePerWorldModData(worldName);
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

    public static void loadBackup(File source, List<WorldSync> whichOnes, String worldName) throws Exception {
        if(!source.exists()){
            TBSync.getLOGGER().error(String.format("Source backup file does not exist for world Backup %s", worldName));
            return;
        }
        Gson gson = new Gson();
        HashMap<WorldSync, String> rawData = gson.fromJson(new FileReader(source), new TypeToken<HashMap<WorldSync, String>>() {}.getType());

        for (WorldSync whichOne : whichOnes) {
            String requiresDecoding = rawData.get(whichOne);
            if(requiresDecoding == null){
                IWorldSyncMap.get(whichOne).loadPerWorldModData(worldName, null);
                continue;
            }
            byte[] rawBytes = Base64.getDecoder().decode(requiresDecoding);
            IWorldSyncMap.get(whichOne).loadPerWorldModData(worldName, new ByteArrayInputStream(rawBytes));
        }
    }
}
