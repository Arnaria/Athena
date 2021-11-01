package arnaria.kingdoms.util;

import net.minecraft.util.math.BlockPos;

public class Parser {

    public static BlockPos blockPosFromString(String pos) {
        String[] values = pos.split(", ");
        return new BlockPos(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }
}
