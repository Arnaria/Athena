package arnaria.kingdoms.util.claims;

import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;

public class ClaimManager {

    private static final Table claimData = new Table("ClaimData");
    private static final ArrayList<Claim> claims = new ArrayList<>();

    public static void addClaim(Claim claim) {
        claims.add(claim);
    }

    public static boolean actionAllowedAt(BlockPos pos, String kingdomId) {
        for (Claim claim : claims) {
            if (claim.contains(pos) && !claim.kingdomId().equals(kingdomId)) return false;
        }
        return true;
    }

    public static ArrayList<Claim> getClaims() {
        return claims;
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
