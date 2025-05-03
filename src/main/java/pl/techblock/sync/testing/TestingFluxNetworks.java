package pl.techblock.sync.testing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pl.techblock.sync.logic.fluxnetworks.FluxNetworks;
import java.io.*;
import java.util.UUID;

public class TestingFluxNetworks {

    private final File targetFile = new File("FluxNetworksTestData.dat");
    private final FluxNetworks fluxNetworks = new FluxNetworks();
    private final UUID testPlayer = UUID.fromString("81a47002-62ad-3ef3-b860-7ec9deeb7837");

    public TestingFluxNetworks(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("DebugFluxNetworksSave")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::save));

        dispatcher.register(
                Commands.literal("DebugFluxNetworksLoad")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::load));

        dispatcher.register(
                Commands.literal("DebugFluxNetworksCleanUp")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::cleanup));
    }

    private int save(CommandContext<CommandSourceStack> commandSource){
        try {
            ByteArrayOutputStream data = fluxNetworks.getSaveData(testPlayer);
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
            fluxNetworks.loadSaveData(testPlayer, inputStream);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    private int cleanup(CommandContext<CommandSourceStack> commandSource){
        fluxNetworks.cleanup(testPlayer);
        return 1;
    }
}