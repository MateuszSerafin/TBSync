package pl.techblock.sync.testing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pl.techblock.sync.logic.enderstorage.EnderStorage;
import java.io.*;
import java.util.UUID;

public class TestingEnderStorage {

    private final EnderStorage enderStorage = new EnderStorage();
    private final UUID testPlayer = UUID.fromString("81a47002-62ad-3ef3-b860-7ec9deeb7837");

    public TestingEnderStorage(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("DebugEnderStorageSave")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::save));

        dispatcher.register(
                Commands.literal("DebugEnderStorageLoad")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::load));

        dispatcher.register(
                Commands.literal("DebugEnderStorageCleanUp")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::cleanup));
    }

    private int save(CommandContext<CommandSourceStack> commandSource){
        enderStorage.savePlayerToDB(testPlayer);
        return 1;
    }

    private int load(CommandContext<CommandSourceStack> commandSource){
        enderStorage.loadPlayerFromDB(testPlayer);
        return 1;
    }

    private int cleanup(CommandContext<CommandSourceStack> commandSource){
        enderStorage.playerCleanUp(testPlayer);
        return 1;
    }
}