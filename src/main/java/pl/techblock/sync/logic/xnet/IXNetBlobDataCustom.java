package pl.techblock.sync.logic.xnet;

import mcjty.xnet.multiblock.WorldBlob;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.Map;

public interface IXNetBlobDataCustom {
    public Map<ResourceKey<Level>, WorldBlob> getWorldBlobMap();
}