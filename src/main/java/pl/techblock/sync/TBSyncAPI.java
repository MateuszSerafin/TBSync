package pl.techblock.sync;

import pl.techblock.sync.api.IPartySync;
import pl.techblock.sync.api.IPlayerSync;
import pl.techblock.sync.api.IWorldSync;
import pl.techblock.sync.api.PartyPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TBSyncAPI {

    protected static final List<IWorldSync> worldSync = new ArrayList<>();
    protected static final List<IPartySync> partySync = new ArrayList<>();
    protected static final List<IPlayerSync> playerSync = new ArrayList<>();

    public static void savePartyToDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        for (IPartySync iPartySync : partySync) {
            iPartySync.savePartyToDB(partyUUID, owner, members);
        }
    }

    public static void loadPartyFromDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        for (IPartySync iPartySync : partySync) {
            iPartySync.loadPartyFromDB(partyUUID, owner, members);
        }
    }

    public static void cleanUpParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) {
        for (IPartySync iPartySync : partySync) {
            iPartySync.cleanUpParty(partyUUID, owner, members);
        }
    }

    public static void addMember(UUID partyUUID, PartyPlayer who) {
        for (IPartySync iPartySync : partySync) {
            iPartySync.addMember(partyUUID, who);
        }
    }

    public static void removeMember(UUID partyUUID, PartyPlayer who) {
        for (IPartySync iPartySync : partySync) {
            iPartySync.removeMember(partyUUID, who);
        }
    }

    public static void savePlayerToDB(UUID playerUUID) {
        for (IPlayerSync iPlayerSync : playerSync) {
            iPlayerSync.savePlayerToDB(playerUUID);
        }
    }

    public static void loadPlayerFromDB(UUID playerUUID) {
        for (IPlayerSync iPlayerSync : playerSync) {
            iPlayerSync.loadPlayerFromDB(playerUUID);
        }
    }

    public static void playerCleanUp(UUID playerUUID) {
        for (IPlayerSync iPlayerSync : playerSync) {
            iPlayerSync.playerCleanUp(playerUUID);
        }
    }

    public static void savePerWorldModDataToDB(String worldName) {
        for (IWorldSync iWorldSync : worldSync) {
            iWorldSync.savePerWorldModDataToDB(worldName);
        }
    }

    public static void loadPerWorldModDataFromDB(String worldName) {
        for (IWorldSync iWorldSync : worldSync) {
            iWorldSync.loadPerWorldModDataFromDB(worldName);
        }
    }

    public static void worldCleanUp(String worldName) {
        for (IWorldSync iWorldSync : worldSync) {
            iWorldSync.worldCleanUp(worldName);
        }
    }
}