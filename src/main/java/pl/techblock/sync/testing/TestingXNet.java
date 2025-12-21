package pl.techblock.sync.testing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pl.techblock.sync.logic.xnet.XNetBlob;
import java.io.*;

public class TestingXNet {

    private final XNetBlob xNetBlob = new XNetBlob();
    private final String worldName = "minecraft:overworld";


    public TestingXNet(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("DebugXNetSave")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::save));

        dispatcher.register(
                Commands.literal("DebugXNetLoad")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::load));

        dispatcher.register(
                Commands.literal("DebugXNetCleanUp")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::cleanup));
    }

    private int save(CommandContext<CommandSourceStack> commandSource){
        xNetBlob.savePerWorldModDataToDB(worldName);
        return 1;
    }

    private int load(CommandContext<CommandSourceStack> commandSource){
        xNetBlob.loadPerWorldModDataFromDB(worldName);
        return 1;
    }

    private int cleanup(CommandContext<CommandSourceStack> commandSource){
        xNetBlob.worldCleanUp(worldName);
        return 1;
    }
}