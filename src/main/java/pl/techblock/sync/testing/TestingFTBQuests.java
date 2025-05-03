package pl.techblock.sync.testing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pl.techblock.sync.logic.ftb.quests.FTBQuests;
import java.io.*;
import java.util.UUID;

public class TestingFTBQuests {

    private final File targetFile = new File("FTBQuestsTestData.dat");
    private final FTBQuests ftbQuests = new FTBQuests();
    private final UUID teamUUID = UUID.fromString("7ed57afd-4c7f-49f6-be50-4bea3ea39045");

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
        try {
            ByteArrayOutputStream data = ftbQuests.getSavePartyData(teamUUID);
            OutputStream outStream = new FileOutputStream(targetFile);
            outStream.write(data.toByteArray());
            outStream.flush();
            outStream.close();
            data.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private int load(CommandContext<CommandSourceStack> commandSource){
        try {
            FileInputStream inputStream = new FileInputStream(targetFile);
            ftbQuests.loadPartyData(teamUUID, inputStream);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    private int cleanup(CommandContext<CommandSourceStack> commandSource){
        ftbQuests.cleanupParty(teamUUID);
        return 1;
    }
}