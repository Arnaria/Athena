package corviolis.athena.mixin;

import corviolis.athena.services.claims.ClaimManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WallBannerBlock.class)
public class WallBannerBlockMixin {

    //Banners that are claim markers can now float
    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    public void sheFloatyNow(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        if (ClaimManager.isClaimMarker(pos)) cir.setReturnValue(state);
    }
}
