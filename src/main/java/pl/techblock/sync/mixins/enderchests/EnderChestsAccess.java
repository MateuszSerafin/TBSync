package pl.techblock.sync.mixins.enderchests;

import com.google.common.collect.Table;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import shetiphian.enderchests.common.misc.ChestHelper;

@Mixin(ChestHelper.class)
public interface EnderChestsAccess {
    @Accessor("DATABASE")
    public static Table getDatabase() {
        throw new AssertionError();
    }
}
