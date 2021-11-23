package arnaria.kingdoms.mixin;

import arnaria.kingdoms.services.claims.NewClaimManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Shadow @Final private List<BlockPos> affectedBlocks;

    @Inject(method = "affectWorld", at = @At("HEAD"))
    public void saveBlocks(boolean particles, CallbackInfo ci) {
        this.affectedBlocks.removeIf(pos -> NewClaimManager.claimExistsAt(new ChunkPos(pos)));
    }
}
