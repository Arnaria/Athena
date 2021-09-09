package arnaria.kingdoms.util.claims;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class ClaimEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (state.getBlock() instanceof PlayerSkullBlock) return true;
            return ClaimManager.actionAllowedAt(pos, player);
        });

        BlockPlaceCallback.EVENT.register((player, context) -> {
            ServerWorld world = (ServerWorld) context.getWorld();
            BlockPos pos = context.getBlockPos();
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof BannerBlock) {
                ClaimManager.addClaim(new Claim(((PlayerEntityInf) player).getKingdomId(), pos));
            }
            return ClaimManager.actionAllowedAt(pos, player);
        });
    }
}
