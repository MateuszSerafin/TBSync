package pl.techblock.sync.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import pl.techblock.sync.api.PlayerManager;
import javax.annotation.Nullable;
import java.util.UUID;

public class DebugPlayers {

    public DebugPlayers(CommandDispatcher<CommandSource> dispatcher){
        dispatcher.register(
                Commands.literal("debugSavePlayer")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .executes(this::save))
        );

        dispatcher.register(
                Commands.literal("debugLoadPlayer")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .executes(this::load))
        );

        dispatcher.register(
                Commands.literal("debugCleanUpPlayer")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .executes(this::cleanup))
        );
    }

    @Nullable
    private UUID checkUUID(String raw){
        try {
            return UUID.fromString(raw);
        } catch (Exception e){
            return null;
        }
    }

    private int save(CommandContext<CommandSource> commandContext){
        UUID uuid = checkUUID(commandContext.getArgument("uuid", String.class));
        if(uuid == null){
            commandContext.getSource().sendFailure(new StringTextComponent("Wrong uuid"));
            return 1;
        }
        PlayerManager.saveAll(uuid);
        return 0;
    }

    private int load(CommandContext<CommandSource> commandContext){
        UUID uuid = checkUUID(commandContext.getArgument("uuid", String.class));
        if(uuid == null){
            commandContext.getSource().sendFailure(new StringTextComponent("Wrong uuid"));
            return 1;
        }
        PlayerManager.loadAll(uuid);
        return 0;
    }

    private int cleanup(CommandContext<CommandSource> commandContext){
        UUID uuid = checkUUID(commandContext.getArgument("uuid", String.class));
        if(uuid == null){
            commandContext.getSource().sendFailure(new StringTextComponent("Wrong uuid"));
            return 1;
        }
        PlayerManager.cleanUpAll(uuid);
        return 0;
    }
}
