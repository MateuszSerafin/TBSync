package pl.techblock.sync.utils;

import java.util.UUID;

//This class really simplifies things. Too many problems without it.
public record PartyPlayer(UUID playerUUID, String playerName){}
