package pl.techblock.sync.testing;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.logic.curios.CuriosExport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.util.UUID;

public class TestingCurios {

    private final UUID testPlayer = UUID.fromString("81a47002-62ad-3ef3-b860-7ec9deeb7837");

    public TestingCurios(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("DebugCuriosSave")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::save));

        dispatcher.register(
                Commands.literal("DebugCuriosLoad")
                        .requires(source -> source.hasPermission(2))
                        .executes(this::load));
        //Yea this is just for testing LGTM!
        DBManager.createTable("CuriosTestingOnly");
    }

    private int save(CommandContext<CommandSourceStack> commandSource){
        try {
            ByteArrayOutputStream data = CuriosExport.saveInventory(testPlayer);
            DBManager.upsertBlob(testPlayer.toString(), "CuriosTestingOnly", new ByteArrayInputStream(data.toByteArray()));
            data.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    private int load(CommandContext<CommandSourceStack> commandSource){
        try {
            Blob data = DBManager.selectBlob(testPlayer.toString(), "CuriosTestingOnly");
            CuriosExport.loadInventory(testPlayer, data.getBinaryStream());
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }
}