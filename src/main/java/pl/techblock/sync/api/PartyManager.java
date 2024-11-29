package pl.techblock.sync.api;

import com.google.gson.internal.LinkedTreeMap;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.enums.PartySync;
import pl.techblock.sync.api.interfaces.IPartySync;
import pl.techblock.sync.logic.ftb.quests.FTBQuests;
import pl.techblock.sync.logic.ftb.teams.FTBTeamsParty;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PartyManager {
    private static Map<PartySync, IPartySync> iPartySyncMap = new LinkedTreeMap<>();

    public static void init(){
        iPartySyncMap.put(PartySync.FTBTeams, new FTBTeamsParty());
        iPartySyncMap.put(PartySync.FTBQuests, new FTBQuests());
    }

    public static void saveAll(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members){
        for (Map.Entry<PartySync, IPartySync> partySyncIPartySyncEntry : iPartySyncMap.entrySet()) {
            PartySync partySync =  partySyncIPartySyncEntry.getKey();
            IPartySync IPartySync = partySyncIPartySyncEntry.getValue();

            try {
                IPartySync.saveParty(partyUUID, owner, members);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to save party data for %s but failed", partyUUID.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void loadAll(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members){
        for (Map.Entry<PartySync, IPartySync> partySyncIPartySyncEntry : iPartySyncMap.entrySet()) {
            PartySync partySync =  partySyncIPartySyncEntry.getKey();
            IPartySync IPartySync = partySyncIPartySyncEntry.getValue();

            try {
                IPartySync.loadParty(partyUUID, owner, members);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to load party data for %s but failed", partyUUID.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void cleanupAll(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members){
        for (Map.Entry<PartySync, IPartySync> partySyncIPartySyncEntry : iPartySyncMap.entrySet()) {
            PartySync partySync =  partySyncIPartySyncEntry.getKey();
            IPartySync IPartySync = partySyncIPartySyncEntry.getValue();

            try {
                IPartySync.cleanupParty(partyUUID, owner, members);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to cleanup party data for %s but failed", partyUUID.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void addMemberToAll(UUID partyUUID,  PartyPlayer who){
        for (Map.Entry<PartySync, IPartySync> partySyncIPartySyncEntry : iPartySyncMap.entrySet()) {
            PartySync partySync =  partySyncIPartySyncEntry.getKey();
            IPartySync IPartySync = partySyncIPartySyncEntry.getValue();

            try {
                IPartySync.addMember(partyUUID, who);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to add member for %s but failed", partyUUID.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void removeMemberFromAll(UUID partyUUID, PartyPlayer who){
        for (Map.Entry<PartySync, IPartySync> partySyncIPartySyncEntry : iPartySyncMap.entrySet()) {
            PartySync partySync =  partySyncIPartySyncEntry.getKey();
            IPartySync IPartySync = partySyncIPartySyncEntry.getValue();

            try {
                IPartySync.removeMember(partyUUID,who);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to remove member for %s but failed", partyUUID.toString()));
                e.printStackTrace();
            }
        }
    }

    @Nullable
    public static IPartySync getSpecific(PartySync what){
        return iPartySyncMap.get(what);
    }
}
