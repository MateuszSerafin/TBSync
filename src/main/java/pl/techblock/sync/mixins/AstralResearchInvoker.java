package pl.techblock.sync.mixins;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(ResearchHelper.class)
public interface AstralResearchInvoker {

    @Invoker("getProgress")
    public static PlayerProgress getProgress(UUID uuid){
        throw new AssertionError();
    };

    @Invoker("load_unsafeFromNBT")
    public static void load_unsafeFromNBT(UUID pUUID, @Nullable CompoundNBT compound){
        throw new AssertionError();
    }
}