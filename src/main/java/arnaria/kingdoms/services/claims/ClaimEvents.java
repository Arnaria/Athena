package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;

public class ClaimEvents {

    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (state.getBlock() instanceof PlayerSkullBlock) return true;
                return ClaimManager.actionAllowedAt(pos, player);
            }
            return true;
        });

        BlockPlaceCallback.EVENT.register((world, player, pos, state, block, item) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (!ClaimManager.actionAllowedAt(pos, player)) return false;

                if (block instanceof BannerBlock bannerBlock) {
                    String kingdomId = ((PlayerEntityInf) player).getKingdomId();
                    if (!kingdomId.isEmpty()) {
                        if (ClaimManager.placedFirstBanner(kingdomId) && !ClaimManager.isClaimInRange(kingdomId, pos)) return false;
                        ClaimManager.addClaim(kingdomId, pos, bannerBlock);
                    }
                }
            }
            return true;
        });
    }
}
