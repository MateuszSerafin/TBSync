package pl.techblock.sync.mixins.futurepack;

import futurepack.api.interfaces.IPlanet;
import futurepack.common.recipes.ISyncedRecipeManager;
import futurepack.common.spaceships.FPPlanetRegistry;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.logic.futurepack.FPPlanetRegistryCustom;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(FPPlanetRegistry.class)
public abstract class FPPlanetRegistryMixin implements ISyncedRecipeManager<IPlanet>, FPPlanetRegistryCustom {

    @Shadow
    private Map<ResourceLocation, IPlanet> DimToPlanet;

    public void removeFromHashMap(ResourceLocation location){
        DimToPlanet.remove(location);
    }

    @Shadow
    public Collection<IPlanet> getRecipes() {
        return List.of();
    }

    @Shadow
    public ResourceLocation getName() {
        return null;
    }
}
