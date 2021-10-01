package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.interfaces.BannerMarkerInf;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.ClaimHelpers;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;
import static arnaria.kingdoms.Kingdoms.database;
import static arnaria.kingdoms.Kingdoms.log;

public class ClaimManager {

    private static final Table claimData = database.createTable("ClaimData");
    private static final ArrayList<Claim> claims = new ArrayList<>();

    public static void init() {
        for (DataContainer claim : claimData.getDataContainers()) {
            String kingdomId = claim.getString("KINGDOM_ID");
            BlockPos pos = claim.getBlockPos("BANNER_POS");
            claims.add(new Claim(kingdomId, pos));

            Block block = overworld.getBlockState(pos).getBlock();
            if (block instanceof BannerBlock bannerBlock) ((BannerMarkerInf) bannerBlock).makeClaimMarker();
            else {
                log(Level.ERROR, "Mismatch in claim data and banner pos!");
                log(Level.ERROR, "The error occurred at: " + pos + ". Suspected BannerBlock, got " + block + ".");
                log(Level.ERROR, "This could have been caused by the server shutting down improperly");
            }
        }
        ClaimEvents.register();
    }

    public static void addClaim(String kingdomId, BlockPos pos, BannerBlock banner) {
        claimData.beginTransaction();
        Claim claim = new Claim(kingdomId, pos);
        claims.add(claim);
        DataContainer claimDataContainer = claimData.createDataContainer(claim.getPos().toString());
        claimData.put(claimDataContainer);
        claimDataContainer.put("KINGDOM_ID", claim.getKingdomId());
        claimDataContainer.put("BANNER_POS", claim.getPos());

        ((BannerMarkerInf) banner).makeClaimMarker();
        KingdomProcedures.addClaimMarkerPointsUsed(kingdomId, 1);
        claimData.endTransaction();
    }

    public static void dropClaim(BlockPos pos) {
        Claim claimToDrop = null;
        for (Claim claim : claims) {
            if (claim.getPos().equals(pos)) {
                claim.removeHologram();
                claimToDrop = claim;
                claimData.drop(pos.toString());
                KingdomProcedures.removeClaimMarkerPointsUsed(claim.getKingdomId(), 1);
                break;
            }
        }

        if (claimToDrop != null) claims.remove(claimToDrop);
    }

    public static void dropClaims(String kingdomId) {
        claimData.beginTransaction();
        ArrayList<Claim> claimsToDrop = new ArrayList<>();
        for (Claim claim : claims) {
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) {
                BlockPos pos = claim.getPos();
                claim.removeHologram();
                claimsToDrop.add(claim);
                claimData.drop(pos.toString());
                if (overworld.getBlockState(pos).getBlock() instanceof BannerBlock) overworld.breakBlock(pos, false);
                KingdomProcedures.removeClaimMarkerPointsUsed(kingdomId, 1);
            }
        }

        claims.removeAll(claimsToDrop);
        claimData.endTransaction();
    }

    public static ArrayList<Claim> getClaims() {
        return claims;
    }

    public static ArrayList<Claim> getClaims(String kingdomId) {
        ArrayList<Claim> kingdomClaims = new ArrayList<>();
        for (Claim claim : claims) if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) kingdomClaims.add(claim);
        return kingdomClaims;
    }

    public static void updateClaimTagColor(String kingdomId, String color) {
        for (Claim claim : claims) {
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) claim.updateColor(color);
        }
    }

    public static boolean actionAllowedAt(BlockPos pos, PlayerEntity player) {
        if (pos.getY() < 0) return true;

        for (Claim claim : claims) {
            if (claim.contains(pos)) {
                if (claim.getKingdomId().equals("ADMIN") && !player.hasPermissionLevel(4)) return false;
                if (!claim.getKingdomId().equals(((PlayerEntityInf) player).getKingdomId())) return false;
            }
        }
        return true;
    }

    public static boolean claimExistsAt(BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.contains(pos)) return true;
        }
        return false;
    }

    public static boolean validBannerPos(String kingdomId, BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.contains(pos)) return false;
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)
                    && !claim.isOverlapping(ClaimHelpers.createChunkBox(pos, 7, false))) return false;
        }
        return true;
    }

    public static boolean placedFirstBanner(String kingdomId) {
        for (Claim claim : claims) {
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) return true;
        }
        return false;
    }

    public static void renderClaims(ServerPlayerEntity player) {
        for (Claim claim : claims) {
            ClaimHelpers.renderClaimEdges(player, claim);
        }
    }

    public static void renderClaimsForPlacement(ServerPlayerEntity player) {
        //ClaimHelpers.renderClaimForPlacement(player, player.getBlockPos(), KingdomsData.getColor(((PlayerEntityInf) player).getKingdomId()));
        for (Claim claim : claims) {
            ClaimHelpers.renderClaimForPlacement(player, claim.getPos(), claim.getColor());
        }
    }

    public static boolean particleAllowedAt(BlockPos pos, BlockPos ignoreClaimPos) {
        for (Claim claim : claims) {
            if (!claim.getPos().equals(ignoreClaimPos) && claim.contains(pos)) return false;
        }
        return true;
    }
}