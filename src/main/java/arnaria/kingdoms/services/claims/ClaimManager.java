package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.ClaimHelpers;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public class ClaimManager {

    private static final Table claimData = new Table("ClaimData");
    private static final ArrayList<Claim> claims = new ArrayList<>();

    public static void addClaim(Claim claim) {
        claims.add(claim);
        DataContainer claimDataContainer = new DataContainer(claim.getPos().toString());
        claimData.put(claimDataContainer);
        claimDataContainer.put("KINGDOM_ID", claim.getKingdomId());
        claimDataContainer.put("BANNER_POS", claim.getPos());
    }

    public static void dropClaim(BlockPos pos) {
        Claim claimToDrop = null;
        for (Claim claim : claims) {
            if (claim.getPos().equals(pos)) {
                claimToDrop = claim;
                DataContainer claimDataContainer = claimData.get(pos.toString());
                claimData.drop(claimDataContainer);
                break;
            }
        }
        if (claimToDrop != null) claims.remove(claimToDrop);
    }

    public static ArrayList<Claim> getClaims() {
        return claims;
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

    public static boolean claimPlacementAllowedAt(BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.contains(pos)) return false;
        }
        return true;
    }

    public static boolean isClaimInRange(String kingdomId, BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.getKingdomId().equals(kingdomId)) {
                return claim.isOverlapping(ClaimHelpers.createChunkBox(pos, 7, false));
            }
        }
        return false;
    }

    public static boolean placedFirstBanner(String kingdomId) {
        for (Claim claim : claims) {
            if (claim.getKingdomId().equals(kingdomId)) return true;
        }
        return false;
    }

    public static boolean isClaimMarker(BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.getPos().equals(pos)) return true;
        }
        return false;
    }

    public static void init() {
        for (String id : claimData.getIds()) {
            DataContainer claim = claimData.get(id);
            String kingdomId = claim.getString("KINGDOM_ID");
            BlockPos pos = claim.getBlockPos("BANNER_POS");
            claims.add(new Claim(kingdomId, pos));
        }
        ClaimEvents.register();
    }
}