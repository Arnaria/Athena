package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.callbacks.BlockPlaceCallback;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class ClaimEvents {

    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                if (state.getBlock() instanceof PlayerSkullBlock) return true;

                if (state.getBlock() instanceof BannerBlock && NewClaimManager.isClaimMarker(pos)) {
                    if (!NewClaimManager.canBreakClaim(pos)) return false;
                }

                return NewClaimManager.actionAllowedAt(pos, player);
            }
            return true;
        });

        BlockPlaceCallback.EVENT.register((world, player, pos, state, block, item) -> {
            if (world.getRegistryKey().equals(World.OVERWORLD)) {
                /*if (!ClaimManager.actionAllowedAt(pos, player)) {
                    NotificationManager.send(player.getUuid(), "You can't place blocks in other kingdoms claims", NotificationTypes.ERROR);
                    return false;
                }

                if (block instanceof BannerBlock bannerBlock) {
                    String kingdomId = ((PlayerEntityInf) player).getKingdomId();
                    if (!kingdomId.isEmpty()) {
                        NbtCompound nbt = item.getNbt();
                        if (nbt != null && nbt.getBoolean("IS_CLAIM_MARKER")) {
                            if (ClaimManager.placedFirstBanner(kingdomId) && !ClaimManager.validBannerPos(kingdomId, pos)) {
                                NotificationManager.send(player.getUuid(), "You can't place a banner here", NotificationTypes.ERROR);
                                return false;
                            }
                            if (ClaimManager.canAffordBanner(kingdomId)) ClaimManager.addClaim(kingdomId, pos, bannerBlock);
                            else NotificationManager.send(player.getUuid(), "Your kingdom does not have enough xp", NotificationTypes.ERROR);
                        }
                    }
                }*/

                if (!NewClaimManager.actionAllowedAt(pos, player)) {
                    NotificationManager.send(player.getUuid(), "You can't place blocks in other kingdoms claims", NotificationTypes.ERROR);
                    return false;
                }

                if (block instanceof BannerBlock) {
                    String kingdomId = ((PlayerEntityInf) player).getKingdomId();
                    if (!kingdomId.isEmpty()) {
                        NbtCompound nbt = item.getNbt();
                        if (nbt != null && nbt.getBoolean("IS_CLAIM_MARKER")) {
                            if (!NewClaimManager.validBannerPos(kingdomId, pos)) {
                                NotificationManager.send(player.getUuid(), "You can't place a banner here", NotificationTypes.ERROR);
                                return false;
                            }

                            if (NewClaimManager.canAffordBanner(kingdomId)) NewClaimManager.addClaim(kingdomId, pos, true);
                            else {
                                NotificationManager.send(player.getUuid(), "Your kingdom does not have enough xp", NotificationTypes.ERROR);
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        });
    }
}
