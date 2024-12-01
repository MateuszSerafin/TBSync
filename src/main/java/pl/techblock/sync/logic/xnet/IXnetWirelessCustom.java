package pl.techblock.sync.logic.xnet;

import mcjty.xnet.multiblock.WirelessChannelKey;
import mcjty.xnet.multiblock.XNetWirelessChannels;
import net.minecraft.nbt.ListNBT;
import java.util.Map;

public interface IXnetWirelessCustom {
    public Map<WirelessChannelKey, XNetWirelessChannels.WirelessChannelInfo> getChannelToWireless();
    public ListNBT exposedWriteRouters(XNetWirelessChannels.WirelessChannelInfo channelInfo);
    public void exposedReadRouters(ListNBT tagList, XNetWirelessChannels.WirelessChannelInfo channelInfo);
}
