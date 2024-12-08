package pl.techblock.sync.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import pl.techblock.sync.api.PartyManager;
import pl.techblock.sync.api.PartyPlayer;
import pl.techblock.sync.api.enums.PartySync;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//could be better
public class DebugParties {

    private UUID partyUUID = UUID.fromString("b259d24f-8a20-4ad9-95a9-6309f5060a63");

    private List<PartyPlayer> members = new ArrayList<>();

    private PartyPlayer owner = new PartyPlayer(UUID.fromString("81a47002-62ad-3ef3-b860-7ec9deeb7837"), "FfFn6XCqhpxY");

    private List<PartySync> synchronizeWhat = new ArrayList<>();

    public DebugParties(CommandDispatcher<CommandSource> dispatcher){
        synchronizeWhat.add(PartySync.FTBTeams);
        synchronizeWhat.add(PartySync.FTBQuests);

        PartyPlayer partymember = new PartyPlayer(UUID.fromString("e21e6b85-7913-3933-8ae4-314fc1e6b863"), "gogoshima");
        members.add(partymember);

        dispatcher.register(
                Commands.literal("debugSaveParty")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::save));

        dispatcher.register(
                Commands.literal("debugLoadParty")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::load));

        dispatcher.register(
                Commands.literal("debugcleaupParty")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::cleanup));

        dispatcher.register(
                Commands.literal("debugaddMember")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::addMember));

        dispatcher.register(
                Commands.literal("debugremoveMember")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::removeMember));

        dispatcher.register(
                Commands.literal("debugmakeBackupParty")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::makeBackup));

        dispatcher.register(
                Commands.literal("debugloadBackupParty")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::loadBackup));
    }

    private int save(CommandContext<CommandSource> commandContext){
        PartyManager.saveSpecificToDB(synchronizeWhat, partyUUID, owner, members);
        return 1;
    }

    private int load(CommandContext<CommandSource> commandContext){
        PartyManager.loadSpecificFromDB(synchronizeWhat, partyUUID, owner, members);
        return 1;
    }

    private int cleanup(CommandContext<CommandSource> commandContext){
        PartyManager.cleanupSpecific(synchronizeWhat, partyUUID, owner, members);
        return 1;
    }

    private int addMember(CommandContext<CommandSource> commandContext){
        PartyManager.addMemberToAll(partyUUID, members.get(0));
        return 1;
    }

    private int removeMember(CommandContext<CommandSource> commandContext){
        PartyManager.removeMemberFromAll(partyUUID, members.get(0));
        return 1;
    }

    public int makeBackup(CommandContext<CommandSource> commandContext){
        try {
            PartyManager.makeBackup(synchronizeWhat, partyUUID, owner, members);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    public int loadBackup(CommandContext<CommandSource> commandContext){
        try {
            PartyManager.loadBackup(new File("./partybackups/testbackup.bak"), synchronizeWhat, partyUUID, owner, members);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }
}
