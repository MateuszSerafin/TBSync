package pl.techblock.sync.mixins.futurepack;

import futurepack.world.dimensions.atmosphere.AtmosphereManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.techblock.sync.logic.futurepack.FuturePackWorldRegistry;
import static futurepack.world.dimensions.atmosphere.AtmosphereManager.hasWorldOxygen;

@Mixin(AtmosphereManager.class)
public abstract class AtmosphereManagerMixin {

    @Inject(method = "addAirCapsToChunk", at = @At("HEAD"), remap = false, cancellable = false)
    public void onTickWorld(AttachCapabilitiesEvent<Chunk> e, CallbackInfo ci) {
        if (e.getGenericType() == Chunk.class) {
            Chunk c = (Chunk)e.getObject();
            if (c.getStatus() == null) {
                return;
            }

            if(hasWorldOxygen(c.getLevel())){
                if(c.getWorldForge().dimension().location().getPath().contains("techislands")){
                    FuturePackWorldRegistry.registerOurWorld(c.getWorldForge().dimension().location().getPath(), 0.2F, 0.4F, false);
                }
            }
        }
    }
}
