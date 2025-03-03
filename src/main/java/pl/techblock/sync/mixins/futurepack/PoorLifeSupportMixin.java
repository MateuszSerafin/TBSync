package pl.techblock.sync.mixins.futurepack;

import futurepack.api.interfaces.IAirSupply;
import futurepack.common.block.modification.machines.TileEntityLifeSupportSystem;
import futurepack.world.dimensions.atmosphere.AtmosphereManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

//flextape
@Mixin(TileEntityLifeSupportSystem.class)
public class PoorLifeSupportMixin {

    @Shadow
    private FluidTank oxygenTank;

    @Inject(method = "updateTile", at = @At("RETURN"), remap = false, cancellable = false)
    public void onTickWorld(int tickCount, CallbackInfo info) {
        TileEntity self = ((TileEntity) (Object) this);
        BlockPos worldPosition = self.getBlockPos();

        List<PlayerEntity> nearbyEntities = self.getLevel().getEntitiesOfClass(PlayerEntity.class, new AxisAlignedBB(worldPosition.getX() - 16, worldPosition.getY() - 16, worldPosition.getZ() - 16, worldPosition.getX() + 16, worldPosition.getY() + 16, worldPosition.getZ() + 16));
        if (nearbyEntities == null) {
            return;
        }
        if (nearbyEntities.isEmpty()) {
            return;
        }
        for (PlayerEntity nearbyEntity : nearbyEntities) {
            fillEntity(nearbyEntity);
        }
    }

    @Unique
    private void fillEntity(PlayerEntity entity){
        LazyOptional<IAirSupply> playerAir = entity.getCapability(AtmosphereManager.cap_AIR);
        if(playerAir.isPresent()){
            IAirSupply playerAirSupply = playerAir.resolve().get();
            int howManyPlayerRequires = playerAirSupply.getMaxAir() - playerAirSupply.getAir();
            int howMuchAirWeHave = oxygenTank.getFluidAmount();
            if(howManyPlayerRequires < 40) return;
            int howMuchCanWeAdd = Math.min(howManyPlayerRequires, howMuchAirWeHave);
            if (howMuchCanWeAdd < 1) return;
            playerAirSupply.addAir(howMuchCanWeAdd);
            oxygenTank.drain(howMuchCanWeAdd, IFluidHandler.FluidAction.EXECUTE);
        }
    }
}