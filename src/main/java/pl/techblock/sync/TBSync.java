package pl.techblock.sync;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.techblock.sync.commands.DebugPlayers;
import pl.techblock.sync.commands.DebugWorlds;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.api.PlayerManager;
import pl.techblock.sync.api.WorldManager;

import java.sql.SQLException;

@Mod("tbsync")
public class TBSync {
    private static final Logger logger = LogManager.getLogger();

    public TBSync() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TBSyncConfig.GENERAL_SPEC, "TBSync.toml");
        MinecraftForge.EVENT_BUS.register(TBSync.class);
    }

    private void setup(FMLCommonSetupEvent event){
        try {
            DBManager.init();
            PlayerManager.init();
            WorldManager.init();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event) {
        new DebugPlayers(event.getDispatcher());
        new DebugWorlds(event.getDispatcher());
    }

    public static Logger getLOGGER() {
        return logger;
    }
}