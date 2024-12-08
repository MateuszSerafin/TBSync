package pl.techblock.sync.api;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.api.enums.PartySync;
import pl.techblock.sync.api.interfaces.IPartySync;
import pl.techblock.sync.logic.ftb.quests.FTBQuests;
import pl.techblock.sync.logic.ftb.teams.FTBTeamsParty;
import javax.annotation.Nullable;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PartyManager {
    private static Map<PartySync, IPartySync> iPartySyncMap = new LinkedTreeMap<>();

    public static void init(){
        iPartySyncMap.put(PartySync.FTBTeams, new FTBTeamsParty());
        iPartySyncMap.put(PartySync.FTBQuests, new FTBQuests());
    }

    public static void saveSpecificToDB(List<PartySync> whichOnes, UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members){
        for (PartySync partySync : whichOnes) {
            IPartySync IPartySync = iPartySyncMap.get(partySync);

            try {
                IPartySync.savePartyToDB(partyUUID, owner, members);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to save party data for %s but failed", partyUUID.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void loadSpecificFromDB(List<PartySync> whichOnes, UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members){
        for (PartySync partySync : whichOnes) {
            IPartySync IPartySync = iPartySyncMap.get(partySync);

            try {
                IPartySync.loadPartyFromDB(partyUUID, owner, members);
            } catch (Exception e){
                TBSync.getLOGGER().error(String.format("Tried to load party data for %s but failed", partyUUID.toString()));
                e.printStackTrace();
            }
        }
    }

    public static void cleanupSpecific(List<PartySync> whichOnes, UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members){
        for (PartySync partySync : whichOnes) {
            IPartySync IPartySync = iPartySyncMap.get(partySync);

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

    public static void makeBackup(List<PartySync> whichOnes, UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception{
        File folder = new File("./partybackups/");
        if(!folder.exists()) folder.mkdir();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");
        String formattedTime = now.format(formatter);
        File destination = new File(folder.toPath() + "/" + partyUUID.toString() + "." + formattedTime + ".bak");

        Map<PartySync, String> gsonThat = new HashMap<>();
        for (PartySync whichOne : whichOnes) {
            ByteArrayOutputStream data = iPartySyncMap.get(whichOne).getSavePartyData(partyUUID, owner, members);
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

    public static void loadBackup(File source, List<PartySync> whichOnes, UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception {
        if(!source.exists()){
            TBSync.getLOGGER().error(String.format("Source backup file does not exist for Party Backup %s", partyUUID.toString()));
            return;
        }
        Gson gson = new Gson();
        HashMap<PartySync, String> rawData = gson.fromJson(new FileReader(source), new TypeToken<HashMap<PartySync, String>>() {}.getType());

        for (PartySync whichOne : whichOnes) {
            String requiresDecoding = rawData.get(whichOne);
            if(requiresDecoding == null){
                iPartySyncMap.get(whichOne).loadPartyData(partyUUID, owner, members, null);
                continue;
            }
            byte[] rawBytes = Base64.getDecoder().decode(requiresDecoding);
            iPartySyncMap.get(whichOne).loadPartyData(partyUUID, owner, members, new ByteArrayInputStream(rawBytes));
        }
    }
}
