package corviolis.kingdoms.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface BlockPlaceCallback {
    Event<BlockPlaceCallback> EVENT = EventFactory.createArrayBacked(BlockPlaceCallback.class,
            (listeners) -> (world, player, pos, state, block, item) -> {
                for (BlockPlaceCallback listener : listeners) {
                    boolean result = listener.place(world, player, pos, state, block, item);

                    if (!result) {
                        return false;
                    }
                }
                return true;
            });

    boolean place(ServerWorld world, PlayerEntity player, BlockPos pos, BlockState state, Block block, ItemStack item);
}