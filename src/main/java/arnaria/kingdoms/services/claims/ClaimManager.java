package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ClaimManager {

    private static final Table claimData = new Table("ClaimData");
    private static final ArrayList<Claim> claims = new ArrayList<>();
    private static int count = 0;

    public static void addClaim(Claim claim) {
        claims.add(claim);
        DataContainer claimDataContainer = new DataContainer(String.valueOf(count));
        claimData.put(claimDataContainer);
        claimDataContainer.put("KINGDOM_ID", claim.getKingdomId());
        claimDataContainer.put("BANNER_POS", claim.getPos());
        count ++;
    }

    public static void dropClaim(BlockPos pos) {
        for (Claim claim : claims) {
            //Cords are split up like this to prevent errors as banners have two block positions (top/bot)
            //We should however check if this is necessary or not
            int claimX = claim.getPos().getX();
            int claimZ = claim.getPos().getZ();
            int posX = pos.getX();
            int posZ = pos.getZ();

            if (claimX == posX && claimZ == posZ) {
                claims.remove(claim);
                DataContainer claimDataContainer = claimData.getWhere("BLOCKPOS", pos);
                claimData.drop(claimDataContainer);
                count--;
            }
        }
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

    public static boolean isClaimMarker(BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.getPos().equals(pos)) return true;
        }
        return false;
    }

    public static void init() {
        List<String> claimIds = claimData.getIds();
        count = claimIds.size();
        for (String id : claimIds) {
            DataContainer claim = claimData.get(id);
            String kingdomId = claim.getString("KINGDOM_ID");
            BlockPos pos = claim.getBlockPos("BANNER_POS");
            claims.add(new Claim(kingdomId, pos));
        }
        ClaimEvents.register();
    }
}