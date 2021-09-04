package arnaria.kingdoms.util.claims;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.SkullBlock;
import net.minecraft.util.ActionResult;

public class ClaimEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (state.getBlock() instanceof SkullBlock) return true;
            return ClaimManager.actionAllowedAt(pos, player);
        });

        BlockPlaceCallback.EVENT.register((player, context) -> {
            if (ClaimManager.actionAllowedAt(context.getBlockPos(), player)) return ActionResult.FAIL;
            return ActionResult.PASS;
        });
    }
}
