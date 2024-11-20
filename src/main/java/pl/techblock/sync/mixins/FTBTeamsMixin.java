package pl.techblock.sync.mixins;

import dev.ftb.mods.ftbteams.data.PlayerTeam;
import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.mods.duckinterfaces.IFTBTeamsCustom;
import java.util.Map;
import java.util.UUID;

@Mixin(TeamManager.class)
public abstract class FTBTeamsMixin implements IFTBTeamsCustom {

    @Shadow
    @Final
    Map<UUID, PlayerTeam> knownPlayers;
    //known players have to be synchronized always

    @Shadow
    @Final
    Map<UUID, Team> teamMap;
    //team map will be loaded only by loading owner of team


    //checked it doesn't matter if it's duplicated
    @Shadow
    Map<String, Team> nameMap;


    @Override
    public Map<UUID, PlayerTeam> knownPlayers() {
        return knownPlayers;
    }

    @Override
    public Map<UUID, Team> teamMap() {
        return teamMap;
    }

    @Override
    public Map<String, Team> nameMap() {
        return nameMap;
    }

    //i leave it for future might be useful
    /*
    @Inject(method = "playerLoggedIn", at = @At("TAIL"), remap = false)
    public void onPlayerLoggedIn(@Nullable ServerPlayerEntity player, UUID id, String name, CallbackInfo callbackInfo){
        System.out.println("That is called");
        for (Map.Entry<UUID, Team> uuidTeamEntry : teamMap.entrySet()) {
            Team team = uuidTeamEntry.getValue();
            if(!(team instanceof PartyTeam)) continue;
            for (UUID member : team.getMembers()) {
                if(member.equals(id)){
                    PlayerTeam pteam = knownPlayers.get(id);
                    knownPlayers.get(id).actualTeam = team;
                    ((IFTBTeamBaseCustom) pteam).getRanks().remove(id);
                    pteam.save();
                    ((PlayerTeam)pteam).updatePresence();
                    //((FTBTeamMixin)(Object) team).changedTeam(pteam, id, player,false);
                    TeamManager.INSTANCE.syncAll();
                    System.out.println("THIS IS DEFINITELY CALLED");

                    break;
                }
            }
        }
    }
     */

    @Overwrite(remap = false)
    public void load(){
        return;
    }

    @Overwrite(remap = false)
    public void saveNow() {
        return;
    }
}
