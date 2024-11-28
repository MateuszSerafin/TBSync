package pl.techblock.sync.mixins.xnet;

import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.xnet.multiblock.XNetWirelessChannels;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(XNetWirelessChannels.class)
public class XNetWirelessChannelsMixin extends AbstractWorldData<XNetWirelessChannels> {

    protected XNetWirelessChannelsMixin(String name) {
        super(name);
    }

    @Override
    public void load(CompoundNBT compoundNBT) {

    }

    @Override
    public CompoundNBT save(CompoundNBT compoundNBT) {
        return null;
    }
}
