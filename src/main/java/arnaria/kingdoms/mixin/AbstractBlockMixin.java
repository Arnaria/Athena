package arnaria.kingdoms.mixin;

import arnaria.kingdoms.interfaces.BannerMarkerInf;
import arnaria.kingdoms.services.claims.NewClaimManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

    @Inject(method = "onStateReplaced", at = @At("HEAD"))
    public void dropClaim(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved, CallbackInfo ci) {
        if (state.getBlock() instanceof BannerBlock) {
            if (!NewClaimManager.getKingdomId(pos).isEmpty()) NewClaimManager.dropClaim(pos);
        }
    }

    /*
     * @author MrNavaStar
     * Im sorry idk what else to do lol
     *//*
    @Deprecated
    @Overwrite
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.hasBlockEntity() && !state.isOf(newState.getBlock())) {
            if (state.getBlock() instanceof BannerBlock bannerBlock) {
                if (!ClaimManager.canBreakClaim(pos)) return;
                if (((BannerMarkerInf) bannerBlock).isClaimMarker()) ClaimManager.dropClaim(pos);
            }
            world.removeBlockEntity(pos);
        }
    }*/
}
