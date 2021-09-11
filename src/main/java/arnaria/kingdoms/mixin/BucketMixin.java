package arnaria.kingdoms.mixin;

import arnaria.kingdoms.systems.claims.ClaimManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(BucketItem.class)

public class BucketMixin extends Item {

    @Shadow
    @Final
    private Fluid fluid;

    public BucketMixin(Settings settings) {
        super(settings);
    }


    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void useOnBlock(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult blockHitResult = raycast(world, user, this.fluid == Fluids.EMPTY ? RaycastContext.FluidHandling.SOURCE_ONLY : RaycastContext.FluidHandling.NONE);
        BlockPos pos = blockHitResult.getBlockPos();

        if (!ClaimManager.actionAllowedAt(pos, user)) {
            cir.setReturnValue(TypedActionResult.fail(itemStack));
        }
    }
}
