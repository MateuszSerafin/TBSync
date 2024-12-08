package pl.techblock.sync.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import pl.techblock.sync.api.PlayerManager;
import pl.techblock.sync.api.enums.PlayerSync;
import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class DebugPlayers {

    private List<PlayerSync> synchonizeWhat = new ArrayList<>();

    public DebugPlayers(CommandDispatcher<CommandSource> dispatcher){
        //synchonizeWhat.add(PlayerSync.AstralResearch);
        synchonizeWhat.add(PlayerSync.FluxNetworks);
        synchonizeWhat.add(PlayerSync.CosmeticArmor);
        synchonizeWhat.add(PlayerSync.FuturePack);
        synchonizeWhat.add(PlayerSync.EnderStorage);
        synchonizeWhat.add(PlayerSync.EnderChests);
        synchonizeWhat.add(PlayerSync.EnderTanks);
        synchonizeWhat.add(PlayerSync.ForgeCaps);

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

        dispatcher.register(
                Commands.literal("debugmakeBackupPlayer")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("uuid", StringArgumentType.string())
                        .executes(this::makeBackup)));

        dispatcher.register(
                Commands.literal("debugloadBackupPlayer")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .then(Commands.argument("file", StringArgumentType.string()).executes(this::loadBackup))));
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
        PlayerManager.saveSpecificToDB(synchonizeWhat, uuid);
        return 0;
    }

    private int load(CommandContext<CommandSource> commandContext){
        UUID uuid = checkUUID(commandContext.getArgument("uuid", String.class));
        if(uuid == null){
            commandContext.getSource().sendFailure(new StringTextComponent("Wrong uuid"));
            return 1;
        }
        PlayerManager.loadSpecificFromDB(synchonizeWhat, uuid);
        return 0;
    }

    private int cleanup(CommandContext<CommandSource> commandContext){
        UUID uuid = checkUUID(commandContext.getArgument("uuid", String.class));
        if(uuid == null){
            commandContext.getSource().sendFailure(new StringTextComponent("Wrong uuid"));
            return 1;
        }
        PlayerManager.cleanUpSpecific(synchonizeWhat, uuid);
        return 0;
    }

    public int makeBackup(CommandContext<CommandSource> commandContext){
        try {
            UUID uuid = checkUUID(commandContext.getArgument("uuid", String.class));
            PlayerManager.makeBackup(synchonizeWhat, uuid);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    public int loadBackup(CommandContext<CommandSource> commandContext){
        try {
            UUID uuid = checkUUID(commandContext.getArgument("uuid", String.class));
            String filePath =  commandContext.getArgument("file", String.class);
            PlayerManager.loadBackup(new File(filePath), synchonizeWhat, uuid);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }
}
