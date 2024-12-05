package pl.techblock.sync.api;

import com.google.gson.internal.LinkedTreeMap;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.enums.WorldSync;
import pl.techblock.sync.api.interfaces.IWorldSync;
import pl.techblock.sync.logic.xnet.XNetBlob;
import java.util.Map;
import java.util.List;

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
}
