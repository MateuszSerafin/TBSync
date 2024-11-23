package pl.techblock.sync.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import shetiphian.enderchests.common.misc.ChestHelper;

@Mixin(ChestHelper.class)
public abstract class EnderChestsMixin {

    @Overwrite(remap = false)
    private void saveChestData(){

    }

    @Overwrite(remap = false)
    public void loadChestData() {

    }
}
