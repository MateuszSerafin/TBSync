package pl.techblock.sync.logic.futurepack;

import futurepack.api.Constants;
import futurepack.common.FuturepackEventHandler;
import futurepack.common.spaceships.FPPlanetRegistry;
import futurepack.common.spaceships.PlanetBase;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
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

    public static void removeFromHashMap(String worldName){
        ((FPPlanetRegistryCustom) FPPlanetRegistry.instance).removeFromHashMap(new ResourceLocation("minecraft", worldName.toLowerCase(Locale.ROOT)));
        ((FuturePackEventHandlerCustom) FuturepackEventHandler.INSTANCE).removeGravityIsland(worldName);
    }

    //ultra flex tape
    private static void changeDimensionForWorld(String worldName){
        RegistryKey<World> worldRegistryKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(worldName));

        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);

        if(world == null) {
            TBSync.getLOGGER().error("Change biome world doesnt exist");
            return;
        }

        DynamicRegistries reg = world.registryAccess();
        DimensionType type = reg.dimensionTypes().get(ASTEROID_BELT_ID);
        world.dimensionType = type;
    }


    private static Biome targetBiome = null;


    public static void changeChunkBiomeSoClientLooksBetter(String worldName, int x, int z){

        RegistryKey<World> worldRegistryKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(worldName));
        ServerWorld world = ServerLifecycleHooks.getCurrentServer().getLevel(worldRegistryKey);

        if(world == null) {
            TBSync.getLOGGER().error("Change biome world doesnt exist");
            return;
        }

        Chunk chunk = world.getChunk(x,z);
        Biome[] biomes = chunk.getBiomes().biomes;

        if(targetBiome == null) {
            DynamicRegistries reg = world.registryAccess();
            ResourceLocation asteroidBeltKey = new ResourceLocation("futurepack", "asteroid_belt");
            MutableRegistry<Biome> a = reg.registry(Registry.BIOME_REGISTRY).get();
            targetBiome =  a.get(asteroidBeltKey);
        }

        //well yes but actually no works good enough <- maybe there is performance hit, but is not as bad when sending update packets
        //yea 2nd thing there is no need for update of chunks
        /*
        boolean RequireChange = false;
        for (int i = 0, biomesLength = biomes.length; i < biomesLength; i++) {
            if(biomes[i].equals(targetBiome)){
                continue;
            }
            RequireChange = true;
        }

         */

        for (int i = 0, biomesLength = biomes.length; i < biomesLength; i++) {
            biomes[i] = targetBiome;
        }
    }
}
