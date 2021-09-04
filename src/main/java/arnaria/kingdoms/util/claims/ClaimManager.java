package arnaria.kingdoms.util.claims;

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
        DataContainer claimData = new DataContainer(String.valueOf(count));
        claimData.put("KINGDOM_ID", claim.kingdomId());
        count ++;
    }

    //Need drop claim method

    public static ArrayList<Claim> getClaims() {
        return claims;
    }

    public static boolean actionAllowedAt(BlockPos pos, PlayerEntity player) {
        for (Claim claim : claims) {
            if (claim.contains(pos)) {
                if (claim.kingdomId().equals("ADMIN") && !player.hasPermissionLevel(4)) return false;
                if (!claim.kingdomId().equals(((PlayerEntityInf) player).getKingdomId())) return false;
            }
        }
        return true;
    }

    public static void init() {
        List<String> claimIds = claimData.getIds();
        count = claimIds.size();
        for (String id : claimIds) {
            String kingdomId = claimData.get(id).getString("KINGDOM_ID");
            claims.add(new Claim(kingdomId));
        }
        ClaimEvents.register();
    }
}