package arnaria.kingdoms.util;

import arnaria.kingdoms.services.claims.Claim;
import arnaria.kingdoms.services.claims.ClaimEdge;
import arnaria.kingdoms.services.claims.ClaimManager;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;

public class ClaimHelpers {

    public static ArrayList<Chunk> createChunkBox(BlockPos center, int diameter, boolean removeOverlapping) {
        ArrayList<Chunk> chunkList = new ArrayList<>();

        int startingOffset = (int) Math.floor((float) diameter / 2) * 16;
        int startX = center.getX() - startingOffset;
        int startZ = center.getZ() - startingOffset;

        int currentX = startX;
        int currentZ = startZ;
        for (int i = 0; i < diameter; i++) {
            for (int j = 0; j < diameter; j++) {
                BlockPos pos = new BlockPos(currentX, 1, currentZ);
                if (removeOverlapping && !ClaimManager.claimExistsAt(pos)) chunkList.add(overworld.getChunk(pos));
                if (!removeOverlapping) chunkList.add(overworld.getChunk(pos));
                currentZ += 16;
            }
            currentX += 16;
            currentZ = startZ;
        }
        return chunkList;
    }

    public static void renderClaimEdges(ServerPlayerEntity player, Claim claim) {
        Formatting color = Formatting.byName(claim.getColor());
        Vec3f rgb = new Vec3f(255, 255, 255);
        if (color != null) rgb = new Vec3f(Vec3d.unpackRgb(color.getColorValue()));

        ParticleEffect effect = new DustParticleEffect(rgb, 2.0F);
        ArrayList<ClaimEdge> edges = claim.getClaimEdges();

        int maxInterval = 5;
        int maxCount = 20;

        for (ClaimEdge edge : edges) {
            int length = edge.length();

            int interval = 1;
            if (length > 0) {
                interval = MathHelper.clamp(length / Math.min(maxCount, length), 1, maxInterval);
            }

            int steps = (length + interval - 1) / interval;
            for (int i = 0; i <= steps; i++) {
                double m = (double) (i * interval) / length;
                spawnParticleIfVisible(player, effect, edge.projX(m), edge.projY(m), edge.projZ(m), claim, 200, "default");
            }
        }
    }

    private static void spawnParticleIfVisible(ServerPlayerEntity player, ParticleEffect effect, double x, double y, double z, Claim claim, int range, String mode) {
        var world = player.getServerWorld();

        var delta = player.getPos().subtract(x, y, z);
        double length2 = delta.lengthSquared();
        if (length2 > range) {
            return;
        }

        var rotation = player.getRotationVec(1.0F);
        double dot = (delta.multiply(1.0 / Math.sqrt(length2))).dotProduct(rotation);
        if (dot > 0.0) {
            return;
        }

        if (!ClaimManager.particleAllowedAt(new BlockPos(x, y, z), claim)) return;
        if (!mode.equals("default") && !((int) y == claim.getPos().getY())) return;

        world.spawnParticles(
                player, effect, true,
                x, y, z,
                1,
                0.0, 0.0, 0.0,
                0.0
        );
    }
}
