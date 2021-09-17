package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.interfaces.BannerMarkerInf;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.util.ClaimHelpers;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.block.BannerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;

public class ClaimManager {

    private static final Table claimData = new Table("ClaimData");
    private static final ArrayList<Claim> claims = new ArrayList<>();

    public static void init() {
        for (String id : claimData.getIds()) {
            DataContainer claim = claimData.get(id);
            String kingdomId = claim.getString("KINGDOM_ID");
            BlockPos pos = claim.getBlockPos("BANNER_POS");
            claims.add(new Claim(kingdomId, pos));

            BannerBlock bannerBlock = (BannerBlock) overworld.getBlockState(pos).getBlock();
            ((BannerMarkerInf) bannerBlock).makeClaimMarker();
        }
        ClaimEvents.register();
    }

    public static void addClaim(String kingdomId, BlockPos pos, BannerBlock banner) {
        Claim claim = new Claim(kingdomId, pos);
        claims.add(claim);
        DataContainer claimDataContainer = new DataContainer(claim.getPos().toString());
        claimData.put(claimDataContainer);
        claimDataContainer.put("KINGDOM_ID", claim.getKingdomId());
        claimDataContainer.put("BANNER_POS", claim.getPos());

        ((BannerMarkerInf) banner).makeClaimMarker();
    }

    public static void dropClaim(BlockPos pos) {
        Claim claimToDrop = null;
        for (Claim claim : claims) {
            if (claim.getPos().equals(pos)) {
                claim.removeHologram();
                claimToDrop = claim;
                claimData.drop(pos.toString());
                break;
            }
        }

        if (claimToDrop != null) claims.remove(claimToDrop);
    }

    public static void dropClaims(String kingdomId) {
        ArrayList<Claim> claimsToDrop = new ArrayList<>();
        for (Claim claim : claims) {
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) {
                claim.removeHologram();
                claimsToDrop.add(claim);
                claimData.drop(claim.getPos().toString());
            }
        }

        claims.removeAll(claimsToDrop);
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
            if (claim.contains(pos)) return false;
        }
        return true;
    }

    public static boolean isClaimInRange(String kingdomId, BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) {
                if (claim.contains(pos)) return false;
                return claim.isOverlapping(ClaimHelpers.createChunkBox(pos, 7, false));
            }
        }
        return false;
    }

    public static boolean placedFirstBanner(String kingdomId) {
        for (Claim claim : claims) {
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) return true;
        }
        return false;
    }
}