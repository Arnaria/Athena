package corviolis.athena.mixin;

import corviolis.athena.services.claims.ClaimManager;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherSkullEntity.class)
public class WitherSkullEntityMixin {

    @Inject(method = "onEntityHit", at = @At("HEAD"), cancellable = true)
    protected void onEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (ClaimManager.claimExistsAt(new ChunkPos(new BlockPos(entityHitResult.getPos())))) {
            if (entityHitResult.getEntity() instanceof PassiveEntity) {
                ci.cancel();
            }
        }
    }
}