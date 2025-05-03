package pl.techblock.sync.mixins.ftb.quests;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.ftb.quests.IFTBQuestsFileCustom;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Mixin(ServerQuestFile.class)
public abstract class FTBServerQuestMixin extends BaseQuestFile implements IFTBQuestsFileCustom {

    @Shadow
    private boolean isLoading;

    @Shadow
    private Path folder;

    @Overwrite(remap = false)
    public void load(){
        this.folder =  new File("./config/").toPath().resolve("ftbquests/quests");
        FTBQuests.LOGGER.info("Loading quests from " + this.folder);
        if (Files.exists(this.folder, new LinkOption[0])) {
            this.isLoading = true;
            this.readDataFull(this.folder, ServerLifecycleHooks.getCurrentServer().registryAccess());
            this.isLoading = false;
        }
        //past this point it was loading each player data not what i want
    }

    @Overwrite(remap = false)
    public void saveNow() {

    }

    @Override
    public Map<UUID, TeamData> getTeamDataMap() {
        return this.teamDataMap;
    }
}