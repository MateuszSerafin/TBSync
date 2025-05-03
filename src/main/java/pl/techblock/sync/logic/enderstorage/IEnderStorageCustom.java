package pl.techblock.sync.logic.enderstorage;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.StorageType;
import java.util.List;
import java.util.Map;

public interface IEnderStorageCustom {
    public Map<String, AbstractEnderStorage> getStorageMap();
    public Map<StorageType<?>, List<AbstractEnderStorage>> getStorageList();
}
