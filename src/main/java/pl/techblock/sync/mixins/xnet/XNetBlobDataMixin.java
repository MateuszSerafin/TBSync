package pl.techblock.sync.mixins.xnet;

import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.mods.duckinterfaces.IXnetBlobDataCustom;

import javax.annotation.Nonnull;
import java.util.Map;

@Mixin(XNetBlobData.class)
public abstract class XNetBlobDataMixin extends AbstractWorldData<XNetBlobData> implements IXnetBlobDataCustom {

    @Shadow
    @Final
    private Map<RegistryKey<World>, WorldBlob> worldBlobMap;

    protected XNetBlobDataMixin(String name) {
        super(name);
    }

    @Override
    public Map<RegistryKey<World>, WorldBlob> getWorldBlobMap() {
        return worldBlobMap;
    }

    @Overwrite(remap = false)
    public CompoundNBT func_189551_b(@Nonnull CompoundNBT compound) {
        return new CompoundNBT();
    }

    @Overwrite(remap = false)
    public void func_76184_a(CompoundNBT compound) {
        return;
    }
}
