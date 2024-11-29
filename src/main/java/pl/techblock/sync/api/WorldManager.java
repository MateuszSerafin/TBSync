package pl.techblock.sync.api;

import com.google.gson.internal.LinkedTreeMap;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.enums.WorldSync;
import pl.techblock.sync.api.interfaces.IWorldSync;
import pl.techblock.sync.logic.xnet.XNetBlob;
import java.util.Map;

//there is assumption that world names do not change, so if you change server
//make sure that world name is exactly the same (it will not work otherwise)
//we use uuid's for world names (as strings)
public class WorldManager {

    private static Map<WorldSync, IWorldSync> IWorldSyncMap = new LinkedTreeMap<>();

    public static void init(){
        IWorldSyncMap.put(WorldSync.XNetBlobData, new XNetBlob());
    }

    public static void saveAll(String worldName){
        for (Map.Entry<WorldSync, IWorldSync> worldSyncIWorldSyncEntry : IWorldSyncMap.entrySet()) {
            WorldSync worldSync =  worldSyncIWorldSyncEntry.getKey();
            IWorldSync IWorldSync = worldSyncIWorldSyncEntry.getValue();

            try {
                IWorldSync.savePerWorldModData(worldName);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to save world data for %s but failed", worldSync.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void loadAll(String worldName){
        for (Map.Entry<WorldSync, IWorldSync> worldSyncIWorldSyncEntry : IWorldSyncMap.entrySet()) {
            WorldSync worldSync =  worldSyncIWorldSyncEntry.getKey();
            IWorldSync IWorldSync = worldSyncIWorldSyncEntry.getValue();

            try {
                IWorldSync.loadPerWorldModData(worldName);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to load world data for %s but failed", worldSync.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void cleanUpAll(String worldName){
        for (Map.Entry<WorldSync, IWorldSync> worldSyncIWorldSyncEntry : IWorldSyncMap.entrySet()) {
            WorldSync worldSync =  worldSyncIWorldSyncEntry.getKey();
            IWorldSync IWorldSync = worldSyncIWorldSyncEntry.getValue();

            try {
                IWorldSync.cleanup(worldName);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to cleanup world data for %s but failed", worldSync.toString()));
                e.printStackTrace();
            }
        }
    }
}
