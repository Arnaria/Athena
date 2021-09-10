package arnaria.kingdoms.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface BlockPlaceCallback {
    Event<BlockPlaceCallback> EVENT = EventFactory.createArrayBacked(BlockPlaceCallback.class,
            (listeners) -> (world, player, pos, block) -> {
                for (BlockPlaceCallback listener : listeners) {
                    boolean result = listener.place(world, player, pos, block);

                    if (!result) {
                        return false;
                    }
                }
                return true;
            });

    boolean place(ServerWorld world, PlayerEntity player, BlockPos pos, Block block);
}