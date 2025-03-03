package pl.techblock.sync.logic.futurepack;

import futurepack.api.Constants;
import futurepack.common.FuturepackEventHandler;
import futurepack.common.spaceships.FPPlanetRegistry;
import futurepack.common.spaceships.PlanetBase;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import pl.techblock.sync.TBSync;
import java.util.Locale;
import static futurepack.depend.api.RegistryCollection.registerPlanet;
import static futurepack.world.dimensions.Dimensions.ASTEROID_BELT_ID;

//we need to register custom world with custom gravities etc.
//realistically it doesn't synchronize anything but if i have mixins in there already it might as well be there
//registering to world registry doesn't do what we want we need to handle gravity additionally also we need to change biomes
public class FuturePackWorldRegistry  {

    public static void registerOurWorld(String worldName, float spread, float gravity, boolean breathable){
        PlanetBase pb = new PlanetBase(new ResourceLocation("minecraft", worldName.toLowerCase(Locale.ROOT)),
                new ResourceLocation(Constants.MOD_ID,"textures/items/armor/modul_healthboost.png"),
                worldName.toLowerCase(Locale.ROOT),
                new String[]{"TechBlockRestricted"})
                .setBreathableAtmosphere(breathable)
                .setOxygenProprties(spread, gravity);
        registerPlanet(pb);
        ((FuturePackEventHandlerCustom) FuturepackEventHandler.INSTANCE).addGravityIsland(worldName);
        changeDimensionForWorld(worldName);
    }

    private static void changeDimensionForWorld(String worldName){
        RegistryKey<World> worldRegistryKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(worldName));
        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);

        if(world == null) {
            TBSync.getLOGGER().error("Change dimensiontype world doesnt exist");
            return;
        }

        DynamicRegistries reg = world.registryAccess();
        DimensionType type = reg.dimensionTypes().get(ASTEROID_BELT_ID);
        world.dimensionType = type;
    }


    public static void removeFromHashMap(String worldName){
        ((FPPlanetRegistryCustom) FPPlanetRegistry.instance).removeFromHashMap(new ResourceLocation("minecraft", worldName.toLowerCase(Locale.ROOT)));
        ((FuturePackEventHandlerCustom) FuturepackEventHandler.INSTANCE).removeGravityIsland(worldName);
    }
}
