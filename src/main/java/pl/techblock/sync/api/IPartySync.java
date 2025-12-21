package pl.techblock.sync.api;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface IPartySync {
    public void savePartyToDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members);
    public void loadPartyFromDB(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members);
    public void cleanUpParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members);

    @Nullable
    public ByteArrayOutputStream saveParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members);
    public void loadParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members, @Nullable InputStream data);

    public void addMember(UUID partyUUID, PartyPlayer who);
    public void removeMember(UUID partyUUID, PartyPlayer who);
}