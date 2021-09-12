package arnaria.kingdoms.mixin;


import arnaria.kingdoms.services.claims.ClaimManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin extends LivingEntity {

    protected ArmorStandEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    public void interactAt(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {

        BlockPos standPos = new BlockPos(hitPos.getX(), hitPos.getX() ,hitPos.getZ());

        if (!ClaimManager.actionAllowedAt(standPos, player)) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getAttacker();

        if (attacker instanceof PlayerEntity) {
            if (!ClaimManager.actionAllowedAt(this.getBlockPos(), (PlayerEntity) attacker)) {
                amount = 0;
            }
        }
    }
}
