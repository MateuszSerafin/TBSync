package pl.techblock.sync.testing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pl.techblock.sync.api.PartyPlayer;
import pl.techblock.sync.logic.ftb.quests.FTBQuests;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TestingFTBQuests {

    private final FTBQuests ftbQuests = new FTBQuests();
    private final UUID teamUUID = UUID.fromString("7ed57afd-4c7f-49f6-be50-4bea3ea39045");
    private final PartyPlayer owner = new PartyPlayer(UUID.fromString("81a47002-62ad-3ef3-b860-7ec9deeb7837"), "FfFn6XCqhpxY");
    private final List<PartyPlayer> members = new ArrayList<>();


    public TestingFTBQuests(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("DebugFTBQuestsSave")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::save));

        dispatcher.register(
                Commands.literal("DebugFTBQuestsLoad")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::load));

        dispatcher.register(
                Commands.literal("DebugFTBQuestsCleanUp")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::cleanup));
    }

    private int save(CommandContext<CommandSourceStack> commandSource){
        ftbQuests.savePartyToDB(teamUUID, owner, members);
        return 1;
    }

    private int load(CommandContext<CommandSourceStack> commandSource){
        ftbQuests.loadPartyFromDB(teamUUID, owner, members);
        return 1;
    }

    private int cleanup(CommandContext<CommandSourceStack> commandSource){
        ftbQuests.cleanUpParty(teamUUID, owner, members);
        return 1;
    }
}