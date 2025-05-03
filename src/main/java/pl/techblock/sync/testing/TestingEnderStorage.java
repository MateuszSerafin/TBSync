package pl.techblock.sync.testing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pl.techblock.sync.logic.enderstorage.EnderStorage;
import java.io.*;
import java.util.UUID;

public class TestingEnderStorage {

    private final File targetFile = new File("EnderStorageTestData.dat");
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
        try {
            ByteArrayOutputStream data = enderStorage.getSaveData(testPlayer);
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
            enderStorage.loadSaveData(testPlayer, inputStream);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    private int cleanup(CommandContext<CommandSourceStack> commandSource){
        enderStorage.cleanup(testPlayer);
        return 1;
    }
}