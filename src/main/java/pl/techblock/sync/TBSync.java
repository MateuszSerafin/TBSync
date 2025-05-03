package pl.techblock.sync;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.techblock.sync.db.DBManager;
import pl.techblock.sync.testing.TestingXNet;
import pl.techblock.sync.testing.TestingFTBQuests;
import pl.techblock.sync.testing.TestingFTBTeams;
import java.sql.SQLException;

@Mod("tbsync")
public class TBSync {

    private static final Logger logger = LogManager.getLogger();

    public TBSync(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommandEvent);
        setup();
    }

    private void setup(){
        try {
            DBManager.init();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void onRegisterCommandEvent(RegisterCommandsEvent event) {
        //those should be disabled in releases
        //new TestingXNet(event.getDispatcher());
        //new TestingFTBQuests(event.getDispatcher());
        //new TestingFTBTeams(event.getDispatcher());
        //new TestingXNet(event.getDispatcher());
    }

    public static Logger getLOGGER() {
        return logger;
    }
}