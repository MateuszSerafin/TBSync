package pl.techblock.sync.testing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pl.techblock.sync.logic.ftb.teams.FTBTeamsParty;
import pl.techblock.sync.utils.PartyPlayer;
import java.util.List;
import java.util.UUID;

public class TestingFTBTeams {

    private final FTBTeamsParty ftbTeams = new FTBTeamsParty();
    private final PartyPlayer owner = new PartyPlayer(UUID.fromString("81a47002-62ad-3ef3-b860-7ec9deeb7837"), "FfFn6XCqhpxY");
    private final List<PartyPlayer> members = List.of(new PartyPlayer(UUID.fromString("47cbe9be-1565-45ea-927a-b73be1df8d99"), "Zbyslaw"));
    private final UUID teamUUID = UUID.fromString("7ed57afd-4c7f-49f6-be50-4bea3ea39045");

    public TestingFTBTeams(CommandDispatcher<CommandSourceStack> dispatcher) {
        //There is no need for saving, teams don't store any data related to quests its separate no need to save

        dispatcher.register(
                Commands.literal("DebugFTBTeamsLoad")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::load));

        dispatcher.register(
                Commands.literal("DebugFTBTeamsCleanUp")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::cleanup));

        dispatcher.register(
                Commands.literal("DebugFTBTeamsAddMember")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::addmember));

        dispatcher.register(
                Commands.literal("DebugFTBTeamsRemoveMember")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::removemember));
    }

    private int load(CommandContext<CommandSourceStack> commandSource){
        try {
            ftbTeams.loadPartyData(teamUUID, owner, members);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private int cleanup(CommandContext<CommandSourceStack> commandSource){
        try {
            ftbTeams.cleanupParty(teamUUID, owner, members);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private int addmember(CommandContext<CommandSourceStack> commandSource){
        try {
            ftbTeams.addMember(teamUUID, members.getFirst());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private int removemember(CommandContext<CommandSourceStack> commandSource){
        try {
            ftbTeams.removeMember(teamUUID, members.getFirst());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
}