package corviolis.athena.mixin;

import corviolis.athena.services.claims.ClaimManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitherEntity.class)
public abstract class WitherEntityMixin extends HostileEntity {

    @Shadow private int blockBreakingCooldown;

    protected WitherEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "mobTick", at = @At("HEAD"))
    public void damage(CallbackInfo ci) {
        if (ClaimManager.claimExistsAt(new ChunkPos(this.getBlockPos()))) {
            this.blockBreakingCooldown = 20;
        }
    }
}
