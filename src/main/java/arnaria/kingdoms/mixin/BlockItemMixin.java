package arnaria.kingdoms.mixin;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import arnaria.kingdoms.interfaces.BannerMarkerInf;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin extends Item {
    @Shadow public abstract Block getBlock();
    @Shadow @Nullable protected abstract BlockState getPlacementState(ItemPlacementContext context);

    public BlockItemMixin(Item.Settings settings) {
        super(settings);
    }

    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    public void dlPlaceEventTrigger(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        boolean result = BlockPlaceCallback.EVENT.invoker().place((ServerWorld) context.getWorld(), context.getPlayer(), context.getBlockPos(), this.getPlacementState(context), this.getBlock(), context.getStack());

        if (!result) {
            ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
            if (player != null) {
                int slot = context.getHand() == Hand.MAIN_HAND ? player.getInventory().selectedSlot : 40;
                ItemStack stack = context.getStack();
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, -2, slot, stack));

                if (this.getBlock() instanceof BannerBlock bannerBlock) {
                    if (((BannerMarkerInf) bannerBlock).isClaimMarker()) {
                        NotificationManager.send(player.getUuid(), "You can't place claim banners inside claims", NotificationTypes.ERROR);
                    }
                } else NotificationManager.send(player.getUuid(), "You can't place blocks in other kingdoms claims", NotificationTypes.ERROR);
            }
            cir.setReturnValue(false);
        }
    }
}