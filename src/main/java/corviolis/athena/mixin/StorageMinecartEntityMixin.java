package corviolis.athena.mixin;

import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import corviolis.athena.services.claims.ClaimManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.StorageMinecartEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StorageMinecartEntity.class)
public abstract class StorageMinecartEntityMixin extends AbstractMinecartEntity {

    protected StorageMinecartEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interact", at = @At("HEAD"))
    public void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!ClaimManager.actionAllowedAt(this.getBlockPos(), player)) {
            NotificationManager.send(player.getUuid(), "You can't use other teams chest minecarts", NotificationTypes.ERROR);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
