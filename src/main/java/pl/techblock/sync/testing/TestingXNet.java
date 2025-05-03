package pl.techblock.sync.testing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pl.techblock.sync.logic.xnet.XNetBlob;
import java.io.*;

public class TestingXNet {

    private final File targetFile = new File("XNetTestData.dat");
    private final XNetBlob xNetBlob = new XNetBlob();
    private final String worldName = "minecraft:the_nether";


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
        try {
            ByteArrayOutputStream data = xNetBlob.savePerWorldModData(worldName);
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
            xNetBlob.loadPerWorldModData(worldName, inputStream);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    private int cleanup(CommandContext<CommandSourceStack> commandSource){
        xNetBlob.cleanup(worldName);
        return 1;
    }
}