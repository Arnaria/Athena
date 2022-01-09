package corviolis.athena.mixin;

import corviolis.athena.services.claims.ClaimManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemFrameItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameItem.class)
public class ItemFrameMixin {

    @Inject(method = "canPlaceOn", at = @At("HEAD"), cancellable = true)
    public void canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!ClaimManager.actionAllowedAt(pos, player)) {
            cir.setReturnValue(false);
        }
    }
}
