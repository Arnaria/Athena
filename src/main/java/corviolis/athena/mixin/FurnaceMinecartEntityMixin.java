package corviolis.athena.mixin;

import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import corviolis.athena.services.claims.ClaimManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FurnaceMinecartEntity.class)
public abstract class FurnaceMinecartEntityMixin extends AbstractMinecartEntity {

    protected FurnaceMinecartEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interact", at = @At("HEAD"))
    public void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!ClaimManager.actionAllowedAt(this.getBlockPos(), player)) {
            NotificationManager.send(player.getUuid(), "You can't use other teams furnace minecarts", NotificationTypes.ERROR);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
