package corviolis.athena.util;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class Parser {

    public static BlockPos stringToBlockpos(String pos) {
        String[] values = pos.split(", ");
        return new BlockPos(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
    }

    public static Vec3f colorNameToRGB(String color) {
        return new Vec3f(Vec3d.unpackRgb(Formatting.byName(color).getColorValue()));
    }
}
