package arnaria.kingdoms.mixin;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.claims.Claim;
import arnaria.kingdoms.services.claims.ClaimManager;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBannerBlock.class)
public class AbstractBannerBlockMixin {

    @Inject(method = "onPlaced", at = @At("HEAD"), cancellable = true)
    public void makeClaim(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack, CallbackInfo ci) {
        if (placer instanceof PlayerEntity) {
            String kingdomId = ((PlayerEntityInf) placer).getKingdomId();
            if (!kingdomId.isEmpty()) {
                //NbtCompound nbt = item.getNbt();
                //if (nbt != null && nbt.getBoolean("IsClaimMarker")) {
                // if (KingdomsData.getClaimMarkerPointsTotal(kingdomId) > KingdomsData.getClaimMarkerPointsUsed(kingdomId)) {
                    if (ClaimManager.placedFirstBanner(kingdomId) && !ClaimManager.isClaimInRange(kingdomId, pos)) ci.cancel();
                    ClaimManager.addClaim(new Claim(kingdomId, pos));
                //}
                //}
            }
        }
    }
}
