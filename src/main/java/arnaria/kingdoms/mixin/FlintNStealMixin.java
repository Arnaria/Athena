package arnaria.kingdoms.mixin;

import arnaria.kingdoms.services.claims.ClaimManager;
import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public class FlintNStealMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity player = context.getPlayer();
        if (player != null && !ClaimManager.actionAllowedAt(context.getBlockPos(), player)) {
            NotificationManager.send(player.getUuid(), "You can't use flint and steel in other kingdoms claims", NotificationTypes.ERROR);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
