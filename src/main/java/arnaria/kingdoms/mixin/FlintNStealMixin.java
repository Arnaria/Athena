package arnaria.kingdoms.mixin;

import arnaria.kingdoms.services.claims.ClaimManager;
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
        if (!ClaimManager.actionAllowedAt(context.getBlockPos(), context.getPlayer())) {
            cir.setReturnValue(ActionResult.FAIL);
        }
    }
}
