package pl.techblock.sync.api.interfaces;

import pl.techblock.sync.api.PartyPlayer;
import java.util.List;
import java.util.UUID;

public interface IPartySync {
    public void saveParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception;
    public void loadParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception;
    public void cleanupParty(UUID partyUUID, PartyPlayer owner, List<PartyPlayer> members) throws Exception;

    public void addMember(UUID partyUUID, PartyPlayer who) throws Exception;
    public void removeMember(UUID partyUUID, PartyPlayer who) throws Exception;
}
