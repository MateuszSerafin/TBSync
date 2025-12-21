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
import pl.techblock.sync.logic.ae2.P2PServiceCustom;
import pl.techblock.sync.logic.enderstorage.EnderStorage;
import pl.techblock.sync.logic.fluxnetworks.FluxNetworks;
import pl.techblock.sync.logic.ftb.quests.FTBQuests;
import pl.techblock.sync.logic.ftb.teams.FTBTeamsParty;
import pl.techblock.sync.logic.xnet.XNetBlob;
import pl.techblock.sync.testing.*;

@Mod("tbsync")
public class TBSync {

    private static final Logger logger = LogManager.getLogger();

    public TBSync(IEventBus modBus, ModContainer container) {
        //currently all commands are just for debug, looks ok
        if (TBSyncConfig.debug) NeoForge.EVENT_BUS.addListener(this::onRegisterCommandEvent);

        DBManager.init();

        //each mod should be tested
        //if this was implemented in first version it would prevent a bug and data corruption
        if (TBSyncConfig.enabledMixins.get("enderstorage")) {
            checkIfClassNameIsLoadedInRunTimeIfNotHardCrash("codechicken.enderstorage.EnderStorage");
            TBSyncAPI.playerSync.add(new EnderStorage());
        }
        if (TBSyncConfig.enabledMixins.get("fluxnetworks")) {
            checkIfClassNameIsLoadedInRunTimeIfNotHardCrash("sonar.fluxnetworks.FluxNetworks");
            TBSyncAPI.playerSync.add(new FluxNetworks());
        }
        if (TBSyncConfig.enabledMixins.get("ftbmixins")) {
            //in this particular case two mods bundled in one mixin "package"
            checkIfClassNameIsLoadedInRunTimeIfNotHardCrash("dev.ftb.mods.ftbquests.FTBQuests");
            checkIfClassNameIsLoadedInRunTimeIfNotHardCrash("dev.ftb.mods.ftbteams.FTBTeams");
            TBSyncAPI.partySync.add(new FTBTeamsParty());
            TBSyncAPI.partySync.add(new FTBQuests());
        }
        if (TBSyncConfig.enabledMixins.get("xnet")) {
            checkIfClassNameIsLoadedInRunTimeIfNotHardCrash("mcjty.xnet.XNet");
            TBSyncAPI.worldSync.add(new XNetBlob());
        }
        if(TBSyncConfig.enabledMixins.get("ae2")){
            checkIfClassNameIsLoadedInRunTimeIfNotHardCrash("appeng.core.AppEngServer");
            P2PServiceCustom.createP2PTable();
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    @SubscribeEvent
    public void onRegisterCommandEvent(RegisterCommandsEvent event) {
        new TestingXNet(event.getDispatcher());
        new TestingFTBQuests(event.getDispatcher());
        new TestingFTBTeams(event.getDispatcher());
        new TestingFluxNetworks(event.getDispatcher());
        new TestingEnderStorage(event.getDispatcher());
    }

    private void checkIfClassNameIsLoadedInRunTimeIfNotHardCrash(String classPath) {
        try {
            Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Required mod not found: " + classPath + ". This is a critical error and the server will now crash.");
        }
    }
}