package arnaria.kingdoms.mixin;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
    public BlockItemMixin(Item.Settings settings) {
        super(settings);
    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    public void dlPlaceEventTrigger(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        ActionResult result = BlockPlaceCallback.EVENT.invoker().place(context.getPlayer(), context);

        if (result != ActionResult.PASS) {
            ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();

            if (player != null) {
                int slot = context.getHand() == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                ItemStack stack = context.getStack();
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, -2, slot, stack));
            }

            cir.setReturnValue(false);
        }
    }
}