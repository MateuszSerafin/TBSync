package pl.techblock.sync.mixins;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.EnderStoragePlugin;
import codechicken.enderstorage.manager.EnderStorageManager;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.mods.duckinterfaces.IEnderStorageCustom;
import java.util.List;
import java.util.Map;

@Mixin(EnderStorageManager.class)
public abstract class EnderStorageMixin implements IEnderStorageCustom {
    //only private player tanks and chests are synchronized
    //prevent loading normal not private frequencies (players will lose items and liquids on those frequencies)
    @Shadow
    private static Map<EnderStorageManager.StorageType<?>, EnderStoragePlugin<?>> plugins;
    @Shadow
    private Map<String, AbstractEnderStorage> storageMap;
    @Shadow
    private Map<EnderStorageManager.StorageType<?>, List<AbstractEnderStorage>> storageList;
    @Shadow
    private List<AbstractEnderStorage> dirtyStorage;

    @Shadow
    private CompoundNBT saveTag;

    @Override
    public Map<String, AbstractEnderStorage> getStorageMap() {
        return this.storageMap;
    }

    @Override
    public Map<EnderStorageManager.StorageType<?>, List<AbstractEnderStorage>> getStorageList() {
        return this.storageList;
    }

    //it needs to be initialized otherwise causes crashes getStorage calls it and expects data
    @Overwrite(remap = false)
    private void load(){
        saveTag =  new CompoundNBT();
    }


    @Overwrite(remap = false)
    private void save(boolean force){

    }

}
