package arnaria.kingdoms.util.claims;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class ClaimEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (state.getBlock() instanceof PlayerSkullBlock) return true;
            if (ClaimManager.actionAllowedAt(pos, player)) {
                if (state.getBlock() instanceof AbstractBannerBlock) {
                    String kingdomId = ((PlayerEntityInf) player).getKingdomId();
                    if (!kingdomId.isEmpty()) ClaimManager.addClaim(new Claim(kingdomId, pos));
                }
                return true;
            }
            return false;
        });

        BlockPlaceCallback.EVENT.register((player, context, block) -> {
            BlockPos pos = context.getBlockPos();
            if (ClaimManager.actionAllowedAt(pos, player)) {
                if (block instanceof AbstractBannerBlock) {
                    String kingdomId = ((PlayerEntityInf) player).getKingdomId();
                    if (!kingdomId.isEmpty()) ClaimManager.addClaim(new Claim(kingdomId, pos));
                }
                return true;
            }
            return false;
        });
    }
}
