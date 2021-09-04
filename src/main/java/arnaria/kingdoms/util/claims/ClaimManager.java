package arnaria.kingdoms.util.claims;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;

public class ClaimManager {

    private static final Table claimData = new Table("ClaimData");
    private static final ArrayList<Claim> claims = new ArrayList<>();

    public static void addClaim(Claim claim) {
        claims.add(claim);
    }

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

    public static void saveData() {
        int count = 0;
        for (Claim claim : claims) {
            DataContainer claimData = new DataContainer(String.valueOf(count));
            claimData.put("KINGDOM_ID", claim.kingdomId());
            count ++;
        }
    }

    public static void init() {
        for (String id : claimData.getIds()) {
            String kingdomId = claimData.get(id).getString("KINGDOM_ID");
            claims.add(new Claim(kingdomId));
        }
        ClaimEvents.register();
    }
}
