package pl.techblock.sync.api.interfaces;

import pl.techblock.sync.api.PartyPlayer;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface IPartySync {
    //internally save loading to db uses the streams
    public void savePartyToDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception;
    public void loadPartyFromDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception;

    @Nullable
    public ByteArrayOutputStream getSavePartyData(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception;
    public void loadPartyData(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members, @Nullable InputStream in) throws Exception;

    public void cleanupParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception;

    public void addMember(UUID partyUUID, PartyPlayer who) throws Exception;
    public void removeMember(UUID partyUUID, PartyPlayer who) throws Exception;
}
