package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.ClaimHelpers;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

public class ClaimEvents {

    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (state.getBlock() instanceof PlayerSkullBlock) return true;
                return ClaimManager.actionAllowedAt(pos, player);
            }
            return true;
        });

        BlockPlaceCallback.EVENT.register((world, player, pos, block, item) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (!ClaimManager.actionAllowedAt(pos, player)) return false;

                if (block instanceof AbstractBannerBlock) {
                    String kingdomId = ((PlayerEntityInf) player).getKingdomId();
                    if (!kingdomId.isEmpty()) {
                        //NbtCompound nbt = item.getNbt();
                        //if (nbt != null && nbt.getBoolean("IsClaimMarker")) {
                        // if (KingdomsData.getClaimMarkerPointsTotal(kingdomId) > KingdomsData.getClaimMarkerPointsUsed(kingdomId)) {
                        if (ClaimManager.placedFirstBanner(kingdomId) && !ClaimManager.isClaimInRange(kingdomId, pos))
                            return false;
                        ClaimManager.addClaim(new Claim(kingdomId, pos));
                        //}
                        //}
                    }
                }
            }
            return true;
        });
    }
}
