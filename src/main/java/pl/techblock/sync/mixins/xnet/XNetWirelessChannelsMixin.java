package pl.techblock.sync.mixins.xnet;

import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.xnet.multiblock.WirelessChannelKey;
import mcjty.xnet.multiblock.XNetWirelessChannels;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.xnet.IXnetWirelessCustom;
import javax.annotation.Nonnull;
import java.util.Map;

//it looks like i fcked up while testing and wireless bit did not require synchronization i am leaving that for future use just in case
//(if it saves data to the world directory i assumed it's important but it looks like channels were working normally by just loading world no idea)
//MIXIN NOT USED
@Deprecated
@Mixin(XNetWirelessChannels.class)
public class XNetWirelessChannelsMixin extends AbstractWorldData<XNetWirelessChannels> implements IXnetWirelessCustom {

    protected XNetWirelessChannelsMixin(String name) {
        super(name);
    }

    @Shadow
    @Final
    private Map<WirelessChannelKey, XNetWirelessChannels.WirelessChannelInfo> channelToWireless;

    @Override
    public Map<WirelessChannelKey, XNetWirelessChannels.WirelessChannelInfo> getChannelToWireless() {
        return channelToWireless;
    }

    @Shadow
    private ListNBT writeRouters(XNetWirelessChannels.WirelessChannelInfo channelInfo) { return null; };


    @Override
    public ListNBT exposedWriteRouters(XNetWirelessChannels.WirelessChannelInfo channelInfo) {
        return writeRouters(channelInfo);
    }

    @Shadow
    private void readRouters(ListNBT tagList, XNetWirelessChannels.WirelessChannelInfo channelInfo) { return; }

    @Override
    public void exposedReadRouters(ListNBT tagList, XNetWirelessChannels.WirelessChannelInfo channelInfo) {
        readRouters(tagList, channelInfo);
    }

    @Overwrite(remap = false)
    public void func_76184_a(CompoundNBT compound) {
        return;
    }

    @Overwrite(remap = false)
    public CompoundNBT func_189551_b(@Nonnull CompoundNBT compound){
        return new CompoundNBT();
    }

    @Override
    public void load(CompoundNBT compoundNBT) {

    }

    @Override
    public CompoundNBT save(CompoundNBT compoundNBT) {
        return new CompoundNBT();
    }
}
