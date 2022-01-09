package corviolis.athena.mixin;

import corviolis.athena.services.claims.ClaimManager;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AxeItem.class)
public class AxeMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = context.getPlayer();
        if (player != null && !ClaimManager.actionAllowedAt(context.getBlockPos(), player)) {
            NotificationManager.send(player.getUuid(), "You can't use axes in other kingdoms claims", NotificationTypes.ERROR);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
