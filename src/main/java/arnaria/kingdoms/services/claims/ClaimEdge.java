package arnaria.kingdoms.services.claims;

import net.minecraft.util.math.MathHelper;

public record ClaimEdge(int startX, int startY, int startZ, int endX, int endY, int endZ) {
    public double projX(double m) {
        return this.startX + (this.endX - this.startX) * m;
    }

    public double projY(double m) {
        return this.startY + (this.endY - this.startY) * m;
    }

    public double projZ(double m) {
        return this.startZ + (this.endZ - this.startZ) * m;
    }

    public int length() {
        int dx = this.endX - this.startX;
        int dy = this.endY - this.startY;
        int dz = this.endZ - this.startZ;
        return MathHelper.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz));
    }
}