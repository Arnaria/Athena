package corviolis.athena.services.claims;

import corviolis.athena.callbacks.BlockPlaceCallback;
import corviolis.athena.interfaces.PlayerEntityInf;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import corviolis.athena.services.data.KingdomsData;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ClaimEvents {

    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {

                //for graves compat
                if (state.getBlock() instanceof PlayerSkullBlock) return true;

                if (!ClaimManager.actionAllowedAt(pos, player)) return false;
                if (state.getBlock() instanceof AbstractBannerBlock && ClaimManager.isClaimMarker(pos)) {
                    if (!ClaimManager.canBreakClaim(pos)) return false;
                    if (((PlayerEntityInf) player).isKing() || KingdomsData.getAdvisers(((PlayerEntityInf) player).getKingdomId()).contains(player.getUuid())) {
                        ClaimManager.dropClaim(pos);
                    }
                }
            }
            return true;
        });

        BlockPlaceCallback.EVENT.register((world, player, pos, state, block, item) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (!ClaimManager.actionAllowedAt(pos, player)) {
                    NotificationManager.send(player.getUuid(), "You can't place blocks in other teams claims", NotificationTypes.ERROR);
                    return false;
                }

                if (block instanceof AbstractBannerBlock) {
                    String kingdomId = ((PlayerEntityInf) player).getKingdomId();
                    if (!kingdomId.isEmpty()) {
                        NbtCompound nbt = item.getNbt();
                        if (nbt != null && nbt.getBoolean("IS_CLAIM_MARKER")) {
                            if (!((PlayerEntityInf)player).isKing() && !KingdomsData.getAdvisers(kingdomId).contains(player.getUuid())) {
                                NotificationManager.send(player.getUuid(), "Only captains and advisors can claim land", NotificationTypes.ERROR);
                                return false;
                            }

                            if (!ClaimManager.validBannerPos(kingdomId, pos)) {
                                NotificationManager.send(player.getUuid(), "You can't place a banner here", NotificationTypes.ERROR);
                                return false;
                            }

                            if (!ClaimManager.canAffordBanner(kingdomId)) {
                                NotificationManager.send(player.getUuid(), "Your team does not have enough xp", NotificationTypes.ERROR);
                                return false;
                            }

                            if (state.getBlock() instanceof WallBannerBlock) ClaimManager.addClaim(kingdomId, pos, true, new Vec3d(0.98, 1, 0.5));
                            else ClaimManager.addClaim(kingdomId, pos, true, new Vec3d(0.5, 2, 0.5));
                        }
                    }
                }
            }
            return true;
        });
    }
}