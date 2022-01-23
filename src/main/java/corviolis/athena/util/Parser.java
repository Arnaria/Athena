package corviolis.athena.util;

import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public class Parser {

    public static Vec3f colorNameToRGB(String color) {
        return new Vec3f(Vec3d.unpackRgb(Formatting.byName(color).getColorValue()));
    }
}
