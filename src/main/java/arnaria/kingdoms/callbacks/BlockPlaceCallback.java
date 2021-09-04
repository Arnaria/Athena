package arnaria.kingdoms.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;

public interface BlockPlaceCallback {
    Event<BlockPlaceCallback> EVENT = EventFactory.createArrayBacked(BlockPlaceCallback.class,
            (listeners) -> (player, context) -> {
                for (BlockPlaceCallback listener : listeners) {
                    ActionResult result = listener.place(player, context);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
            });

    ActionResult place(PlayerEntity player, ItemPlacementContext context);
}