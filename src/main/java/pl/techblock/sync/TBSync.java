package pl.techblock.sync;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.techblock.sync.db.DBManager;
import java.sql.SQLException;

@Mod("tbsync")
public class TBSync {
    private static final Logger logger = LogManager.getLogger();

    public TBSync() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(TBSync.class);
    }

    private void setup(FMLCommonSetupEvent event){
        try {
            DBManager.init();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event) {
    }

    public static Logger getLOGGER() {
        return logger;
    }
}