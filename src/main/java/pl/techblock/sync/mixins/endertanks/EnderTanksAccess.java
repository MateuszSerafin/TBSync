package pl.techblock.sync.mixins.endertanks;

import com.google.common.collect.Table;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import shetiphian.endertanks.common.misc.EnderContainer;
import shetiphian.endertanks.common.misc.TankHelper;

@Mixin(TankHelper.class)
public interface EnderTanksAccess {
    @Accessor("DATABASE")
    public static Table<String, String, EnderContainer> getDatabase() {
        throw new AssertionError();
    }
}
