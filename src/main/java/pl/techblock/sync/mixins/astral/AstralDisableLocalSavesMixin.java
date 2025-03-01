package pl.techblock.sync.mixins.astral;

import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import java.util.UUID;

@Mixin(ResearchHelper.class)
public abstract class AstralDisableLocalSavesMixin {

    @Overwrite(remap = false)
    private static void savePlayerKnowledge(UUID pUUID, boolean force){
        return;
    }

    //if that is called, it tries to load local so we can just reset it
    @Overwrite(remap = false)
    private static void loadPlayerKnowledge(UUID pUUID) {
        AstralResearchAccess.load_unsafeFromNBT(pUUID, null);
        return;
    }

}
