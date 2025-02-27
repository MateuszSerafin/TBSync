package pl.techblock.sync.mixins.futurepack;

import futurepack.common.FuturepackEventHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.techblock.sync.logic.futurepack.FuturePackEventHandlerCustom;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Mixin(FuturepackEventHandler.class)
public class FuturePackEventHandlerMixin implements FuturePackEventHandlerCustom {

    //this set will tick, therefore it needs to be perhaps more optimized we will see
    @Unique
    private Set<String> worldsWhereShouldBeGravity = new TreeSet<>();

    @Unique
    public void addGravityIsland(String world){
        worldsWhereShouldBeGravity.add(world);
    }

    @Unique
    public void removeGravityIsland(String world){
        worldsWhereShouldBeGravity.remove(world);
    }

    @Inject(method = "onLivingUpdate", at = @At("RETURN"), remap = false, cancellable = false)
    public void onTickWorld(LivingEvent.LivingUpdateEvent event, CallbackInfo info) {
        /*
        #client side check not my problem
        if (!event.getEntity().func_130014_f_().field_72995_K) {
        #this is called anyway
        AttributeModifier GRAVITY = new AttributeModifier(MICRO_GRAVITY_ID, "MICRO GRAVITY", -0.875, AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (liv.func_110148_a((Attribute) ForgeMod.ENTITY_GRAVITY.get()).func_180374_a(GRAVITY)) {
            liv.func_110148_a((Attribute)ForgeMod.ENTITY_GRAVITY.get()).func_111124_b(GRAVITY);
        }
        */
        LivingEntity livingEntity = event.getEntityLiving();

        String livingEntityWorld = livingEntity.level.dimension().location().getPath();

        if(!worldsWhereShouldBeGravity.contains(livingEntityWorld)){
            return;
        }
        UUID MICRO_GRAVITY_ID = UUID.fromString("A1B69F2A-2F7C-31EF-9022-7C4E7D5E2ABA");
        AttributeModifier GRAVITY = new AttributeModifier(MICRO_GRAVITY_ID, "MICRO GRAVITY", -0.875, AttributeModifier.Operation.MULTIPLY_TOTAL);
        livingEntity.getAttribute(ForgeMod.ENTITY_GRAVITY.get()).addTransientModifier(GRAVITY);
        livingEntity.fallDistance = 0.0F;
    }

}
