package pl.techblock.sync.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import pl.techblock.sync.logic.PlayerManager;
import java.util.UUID;

public class DebugCleanUpPlayer {
    public DebugCleanUpPlayer(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("debugCleanUpPlayer")
                        .requires(source -> source.hasPermission(2))
                        .executes(DebugCleanUpPlayer::debugCleanUpPlayer)
        );
    }

    public static int debugCleanUpPlayer(CommandContext<CommandSource> commandContext){
        CommandSource source = commandContext.getSource();
        ServerPlayerEntity player;
        //not executed as player fail
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            return 1;
        }

        UUID pUUID = player.getUUID();
        PlayerManager.cleanUpAll(pUUID);
        return 0;
    }
}
