package arnaria.kingdoms.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;

public interface BlockPlaceCallback {
    Event<BlockPlaceCallback> EVENT = EventFactory.createArrayBacked(BlockPlaceCallback.class,
            (listeners) -> (player, context, block) -> {
                for (BlockPlaceCallback listener : listeners) {
                    boolean result = listener.place(player, context, block);

                    if (!result) {
                        return false;
                    }
                }
                return true;
            });

    boolean place(PlayerEntity player, ItemPlacementContext context, Block block);
}