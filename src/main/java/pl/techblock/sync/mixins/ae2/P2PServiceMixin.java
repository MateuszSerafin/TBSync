package pl.techblock.sync.mixins.ae2;

import appeng.me.service.P2PService;
import appeng.parts.p2p.P2PTunnelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import pl.techblock.sync.TBSync;
import pl.techblock.sync.logic.ae2.P2PServiceCustom;
import java.util.HashMap;
import java.util.Random;

@Mixin(P2PService.class)
public class P2PServiceMixin {

    @Shadow
    private Random frequencyGenerator;

    @Shadow
    private final HashMap<Short, P2PTunnelPart<?>> inputs = new HashMap();

    @Overwrite(remap = false)
    public short newFrequency() {
        short p2pSafeID = (short) (P2PServiceCustom.getp2Pidincrementallybetweensservers() - 32768);
        TBSync.getLogger().info("Generating P2PID... it's {}", String.valueOf(p2pSafeID));
        return p2pSafeID;
    }
}