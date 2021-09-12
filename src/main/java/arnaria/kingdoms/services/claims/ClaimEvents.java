package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.ClaimHelpers;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

public class ClaimEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (state.getBlock() instanceof PlayerSkullBlock) return true;
                if (!ClaimManager.actionAllowedAt(pos, player)) return false;
                if (state.getBlock() instanceof AbstractBannerBlock && ClaimManager.isClaimMarker(pos)) ClaimManager.dropClaim(pos);
            }
            return true;
        });

        BlockPlaceCallback.EVENT.register((world, player, pos, block, item) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (!ClaimManager.actionAllowedAt(pos, player)) return false;

                String kingdomId = ((PlayerEntityInf) player).getKingdomId();
                if (block instanceof AbstractBannerBlock && !kingdomId.isEmpty()) {
                    NbtCompound nbt = item.getNbt();
                    if (nbt != null && nbt.getBoolean("IsClaimMarker")) {
                        if (KingdomsData.getClaimMarkerPointsTotal(kingdomId) > KingdomsData.getClaimMarkerPointsUsed(kingdomId)) {
                            if (ClaimManager.isClaimInRange(kingdomId, pos)) ClaimManager.addClaim(new Claim(kingdomId, pos));
                        }
                    }
                }
            }
            return true;
        });
    }
}
