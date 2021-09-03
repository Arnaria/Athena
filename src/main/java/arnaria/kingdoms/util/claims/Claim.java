package arnaria.kingdoms.util.claims;

import net.minecraft.util.math.BlockPos;

public record Claim(String kingdomId) {

    public boolean contains(BlockPos pos) {
        int x = pos.getX();
        int z = pos.getZ();
        return false; //(((x1 < x || x1 == x) && (x < x2 || x == x2)) && ((z1 < z || z1 == z) && (z < z2 || z == z2)));
    }
}
