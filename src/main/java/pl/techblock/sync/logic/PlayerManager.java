package pl.techblock.sync.logic;

import com.google.gson.internal.LinkedTreeMap;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.logic.enums.PlayerSync;
import pl.techblock.sync.logic.interfaces.IPlayerSync;
import pl.techblock.sync.mods.players.Test;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private static Map<PlayerSync, IPlayerSync> IPlayerSyncMap = new LinkedTreeMap<>();

    public static void innit(){
        IPlayerSyncMap.put(PlayerSync.TEST, new Test());
    }

    public static void saveAll(UUID playerUUID){
        for (Map.Entry<PlayerSync, IPlayerSync> playerSyncIPlayerSyncEntry : IPlayerSyncMap.entrySet()) {
            PlayerSync playerSync =  playerSyncIPlayerSyncEntry.getKey();
            IPlayerSync IPlayerSync = playerSyncIPlayerSyncEntry.getValue();

            try {
                IPlayerSync.saveToDB(playerUUID);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to save player data for %s but failed", playerSync.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void loadAll(UUID playerUUID){
        for (Map.Entry<PlayerSync, IPlayerSync> playerSyncIPlayerSyncEntry : IPlayerSyncMap.entrySet()) {
            PlayerSync playerSync =  playerSyncIPlayerSyncEntry.getKey();
            IPlayerSync IPlayerSync = playerSyncIPlayerSyncEntry.getValue();
            try {
                IPlayerSync.loadFromDB(playerUUID);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to load player data for %s but failed", playerSync.toString()));
                e.printStackTrace();
            }
        }
    }

    //all of enums should be implemented
    public static IPlayerSync getSpecific(PlayerSync what){
        return IPlayerSyncMap.get(what);
    }
}