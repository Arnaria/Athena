package arnaria.kingdoms.mixin;

import arnaria.kingdoms.services.claims.ClaimManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
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
            BlockEntity bannerBlock = ((BannerBlock) state.getBlock()).createBlockEntity(pos, state);

            if (bannerBlock != null) {
                NbtCompound nbt = bannerBlock.writeNbt(new NbtCompound());
                System.out.println(nbt);
                if (nbt.getBoolean("IS_CLAIM_MARKER")) ClaimManager.dropClaim(pos);
            }
        }
    }
}
