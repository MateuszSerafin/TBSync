package pl.techblock.sync.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import pl.techblock.sync.logic.PlayerManager;
import java.util.UUID;

public class DebugSavePlayer {
    public DebugSavePlayer(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("debugSavePlayer")
                        .requires(source -> source.hasPermission(2))
                        .executes(DebugSavePlayer::debugSavePlayer)
        );
    }

    public static int debugSavePlayer(CommandContext<CommandSource> commandContext){
        CommandSource source = commandContext.getSource();
        ServerPlayerEntity player;
        //not executed as player fail
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
           return 1;
        }

        UUID pUUID = player.getUUID();
        PlayerManager.saveAll(pUUID);
        return 0;
    }
}