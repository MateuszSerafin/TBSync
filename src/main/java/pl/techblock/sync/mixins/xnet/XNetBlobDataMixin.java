package pl.techblock.sync.mixins.xnet;

import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.techblock.sync.logic.xnet.IXNetBlobDataCustom;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@Mixin(XNetBlobData.class)
public abstract class XNetBlobDataMixin extends AbstractWorldData<XNetBlobData> implements IXNetBlobDataCustom {

    @Shadow
    @Final
    private Map<ResourceKey<Level>, WorldBlob> worldBlobMap = new HashMap();

    //no idea how to overwrite constructor,
    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "RETURN"), remap = false)
    private void preventLoadingData(CompoundTag tag, CallbackInfo ci){
        this.worldBlobMap.clear();
    }

    @Override
    public Map<ResourceKey<Level>, WorldBlob> getWorldBlobMap() {
        return worldBlobMap;
    }

    @Overwrite(remap = false)
    public CompoundTag save(@Nonnull CompoundTag compound, HolderLookup.Provider provider) {
        return new CompoundTag();
    }
}