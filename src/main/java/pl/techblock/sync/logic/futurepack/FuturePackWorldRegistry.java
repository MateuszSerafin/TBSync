package pl.techblock.sync.logic.futurepack;

import futurepack.api.Constants;
import futurepack.common.spaceships.FPPlanetRegistry;
import futurepack.common.spaceships.PlanetBase;
import net.minecraft.util.ResourceLocation;
import java.util.Locale;
import static futurepack.depend.api.RegistryCollection.registerPlanet;

//we need to register custom world with custom gravities etc.
//realistically it doesn't synchronize anything but if i have mixins in there already it might as well be there
public class FuturePackWorldRegistry  {

    public static void registerOurWorld(String worldName, float spread, float gravity, boolean breathable){
        PlanetBase pb = new PlanetBase(new ResourceLocation("minecraft", worldName.toLowerCase(Locale.ROOT)),
                new ResourceLocation(Constants.MOD_ID,"textures/items/armor/modul_healthboost.png"),
                worldName.toLowerCase(Locale.ROOT),
                new String[]{"TechBlockRestricted"})
                .setBreathableAtmosphere(breathable)
                .setOxygenProprties(spread, gravity);
        registerPlanet(pb);
    }

    public static void removeFromHashMap(String worldName){
        ((FPPlanetRegistryCustom) FPPlanetRegistry.instance).removeFromHashMap(new ResourceLocation("minecraft", worldName.toLowerCase(Locale.ROOT)));
    }
}
