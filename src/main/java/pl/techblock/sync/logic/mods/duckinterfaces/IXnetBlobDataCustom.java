package pl.techblock.sync.logic.mods.duckinterfaces;

import mcjty.xnet.multiblock.WorldBlob;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import java.util.Map;

public interface IXnetBlobDataCustom {
    public Map<RegistryKey<World>, WorldBlob> getWorldBlobMap();
}
