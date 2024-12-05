package pl.techblock.sync.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import pl.techblock.sync.api.WorldManager;
import pl.techblock.sync.api.enums.WorldSync;
import java.util.List;
import java.util.ArrayList;

public class DebugWorlds {

    private List<WorldSync> synchronizeWhat = new ArrayList<>();

    public DebugWorlds(CommandDispatcher<CommandSource> dispatcher){
        synchronizeWhat.add(WorldSync.XNetBlobData);

        dispatcher.register(
                Commands.literal("debugSaveWorld")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("worldName", StringArgumentType.string())
                                .executes(this::save))
        );

        dispatcher.register(
                Commands.literal("debugLoadWorld")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("worldName", StringArgumentType.string())
                                .executes(this::load))
        );

        dispatcher.register(
                Commands.literal("debugCleanUpWorld")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("worldName", StringArgumentType.string())
                                .executes(this::cleanup))
        );
    }

    private int save(CommandContext<CommandSource> commandContext){
        String raw  = commandContext.getArgument("worldName", String.class);
        WorldManager.saveSpecificToDB(synchronizeWhat, raw);
        return 0;
    }

    private int load(CommandContext<CommandSource> commandContext){
        String raw  = commandContext.getArgument("worldName", String.class);
        WorldManager.loadSpecificFromDB(synchronizeWhat, raw);
        return 0;
    }

    private int cleanup(CommandContext<CommandSource> commandContext){
        String raw  = commandContext.getArgument("worldName", String.class);
        WorldManager.cleanUpSpecific(synchronizeWhat, raw);
        return 0;
    }
}
