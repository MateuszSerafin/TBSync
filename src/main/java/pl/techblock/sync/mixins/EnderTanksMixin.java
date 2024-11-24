package pl.techblock.sync.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import shetiphian.endertanks.common.misc.TankHelper;

@Mixin(TankHelper.class)
public abstract class EnderTanksMixin {

    @Overwrite(remap = false)
    private void saveTankData(){
        return;
    }

    @Overwrite(remap = false)
    public void loadTankData(){
        return;
    }

}
