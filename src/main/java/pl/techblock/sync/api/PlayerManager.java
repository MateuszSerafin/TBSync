package pl.techblock.sync.api;

import com.google.gson.internal.LinkedTreeMap;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.enums.PlayerSync;
import pl.techblock.sync.api.interfaces.IPlayerSync;
import pl.techblock.sync.logic.astral.AstralResearch;
import pl.techblock.sync.logic.cosmeticarmor.CosmeticArmor;
import pl.techblock.sync.logic.enderchests.EnderChests;
import pl.techblock.sync.logic.enderstorage.EnderStorage;
import pl.techblock.sync.logic.endertanks.EnderTanks;
import pl.techblock.sync.logic.fluxnetworks.FluxNetworks;
import pl.techblock.sync.logic.futurepack.FuturePack;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

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

    public static void cleanUpAll(UUID playerUUID){
        for (Map.Entry<PlayerSync, IPlayerSync> playerSyncIPlayerSyncEntry : IPlayerSyncMap.entrySet()) {
            PlayerSync playerSync =  playerSyncIPlayerSyncEntry.getKey();
            IPlayerSync IPlayerSync = playerSyncIPlayerSyncEntry.getValue();
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
}