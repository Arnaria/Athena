package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.interfaces.BannerMarkerInf;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.ClaimHelpers;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
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
        Claim kingdomClaim = new Claim(kingdomId, pos);
        claims.add(kingdomClaim);

        claimData.beginTransaction();
        DataContainer claimDataContainer = claimData.createDataContainer(kingdomClaim.getPos().toShortString());
        claimDataContainer.put("KINGDOM_ID", kingdomClaim.getKingdomId());
        claimDataContainer.put("BANNER_POS", kingdomClaim.getPos());
        claimData.endTransaction();

        ((BannerMarkerInf) banner).makeClaimMarker();
        KingdomProcedures.addToBannerCount(kingdomId, 1);
        if (!placedFirstBanner(kingdomId)) KingdomProcedures.setStartingBannerPos(kingdomId, pos);
    }

    public static void dropClaim(BlockPos pos) {
        Claim kingdomClaimToDrop = null;
        for (Claim kingdomClaim : claims) {
            if (kingdomClaim.getPos().equals(pos)) {
                kingdomClaim.removeHologram();
                kingdomClaimToDrop = kingdomClaim;
                claimData.drop(pos.toShortString());
                KingdomProcedures.removeFromBannerCount(kingdomClaim.getKingdomId(), 1);
                break;
            }
        }

        if (kingdomClaimToDrop != null) claims.remove(kingdomClaimToDrop);
    }

    //Only use when dropping kingdoms
    public static void dropClaims(String kingdomId) {
        claimData.beginTransaction();
        ArrayList<Claim> claimsToDrop = new ArrayList<>();
        for (Claim kingdomClaim : claims) {
            if (kingdomClaim.getKingdomId().equalsIgnoreCase(kingdomId)) {
                BlockPos pos = kingdomClaim.getPos();
                kingdomClaim.removeHologram();
                claimsToDrop.add(kingdomClaim);
                claimData.drop(pos.toShortString());
                if (overworld.getBlockState(pos).getBlock() instanceof BannerBlock) overworld.breakBlock(pos, false);
            }
        }
        claimData.endTransaction();
        claims.removeAll(claimsToDrop);
    }

    public static ArrayList<Claim> getClaims() {
        return claims;
    }

    public static ArrayList<Claim> getClaims(String kingdomId) {
        ArrayList<Claim> kingdomKingdomClaims = new ArrayList<>();
        for (Claim kingdomClaim : claims) if (kingdomClaim.getKingdomId().equalsIgnoreCase(kingdomId)) kingdomKingdomClaims.add(kingdomClaim);
        return kingdomKingdomClaims;
    }

    public static void updateClaimTagColor(String kingdomId, String color) {
        for (Claim kingdomClaim : claims) {
            if (kingdomClaim.getKingdomId().equalsIgnoreCase(kingdomId)) kingdomClaim.updateColor(color);
        }
    }

    public static boolean actionAllowedAt(BlockPos pos, PlayerEntity player) {
        if (pos.getY() < 0) return true;

        for (Claim kingdomClaim : claims) {
            if (kingdomClaim.contains(pos)) {
                if (kingdomClaim.getKingdomId().equals("ADMIN") && !player.hasPermissionLevel(4)) return false;
                if (!kingdomClaim.getKingdomId().equals(((PlayerEntityInf) player).getKingdomId())) return false;
            }
        }
        return true;
    }

    public static boolean claimExistsAt(BlockPos pos) {
        for (Claim kingdomClaim : claims) {
            if (kingdomClaim.contains(pos)) return true;
        }
        return false;
    }

    public static boolean validBannerPos(String kingdomId, BlockPos pos) {
        for (Claim kingdomClaim : claims) {
            if (kingdomClaim.contains(pos)) return false;
            if (kingdomClaim.getKingdomId().equalsIgnoreCase(kingdomId)
                    && !kingdomClaim.isOverlapping(ClaimHelpers.createChunkBox(pos, 7, false))) return false;
        }
        return true;
    }

    public static boolean placedFirstBanner(String kingdomId) {
        return KingdomsData.getBannerCount(kingdomId) > 0;
    }

    public static void renderClaims(ServerPlayerEntity player) {
        for (Claim kingdomClaim : claims) ClaimHelpers.renderClaim(player, kingdomClaim);
    }

    public static void renderClaimsForPlacement(ServerPlayerEntity player) {
        if (!validBannerPos(((PlayerEntityInf) player).getKingdomId(), player.getBlockPos())) ClaimHelpers.renderClaimLayer(player, player.getBlockPos(), (int) player.getY(), Formatting.WHITE, 256 * 256);
        for (Claim kingdomClaim : claims) {
            ClaimHelpers.renderClaimLayer(player, kingdomClaim.getPos(), (int) player.getY(), Formatting.byName(kingdomClaim.getColor()), 256 * 256);
        }
    }

    public static boolean canAffordBanner(String kingdomId) {
        int bannersAllowed = (int) Math.floor((float) KingdomsData.getXp(kingdomId) / 1000) + 1;
        return bannersAllowed > KingdomsData.getBannerCount(kingdomId);
    }
}