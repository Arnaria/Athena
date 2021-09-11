package arnaria.kingdoms.util.claims;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import arnaria.kingdoms.interfaces.BannerBlockInf;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.world.World;

public class ClaimEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (state.getBlock() instanceof PlayerSkullBlock) return true;
                if (!ClaimManager.actionAllowedAt(pos, player)) return false;
                if (state.getBlock() instanceof AbstractBannerBlock) {
                    if (((BannerBlockInf) state.getBlock()).isClaimMarker()) ClaimManager.dropClaim(pos);
                }
            }
            return true;
        });

        BlockPlaceCallback.EVENT.register((world, player, pos, block) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (!ClaimManager.actionAllowedAt(pos, player)) return false;
                if (block instanceof AbstractBannerBlock && ((BannerBlockInf) block).isClaimMarker()) {
                    String kingdomId = ((PlayerEntityInf) player).getKingdomId();
                    if (!kingdomId.isEmpty()) ClaimManager.addClaim(new Claim(kingdomId, pos));
                }
            }
            return true;
        });
    }
}
