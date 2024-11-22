package pl.techblock.sync.logic.mods.duckinterfaces;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.manager.EnderStorageManager;
import java.util.List;
import java.util.Map;

public interface IEnderStorageCustom {
    public Map<String, AbstractEnderStorage> getStorageMap();
    public Map<EnderStorageManager.StorageType<?>, List<AbstractEnderStorage>> getStorageList();
}
