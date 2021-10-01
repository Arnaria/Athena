package arnaria.kingdoms.util;

import arnaria.kingdoms.services.claims.Claim;
import arnaria.kingdoms.services.claims.ClaimEdge;
import arnaria.kingdoms.services.claims.ClaimManager;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
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

    public static BlockPos[] getCorners(BlockPos center) {
        return new BlockPos[] {
            overworld.getChunk(center.add(-32, 0, -32)).getPos().getStartPos().add(-1, 0, -1),
            overworld.getChunk(center.add(32, 0, 32)).getPos().getStartPos().add(17, 0, 17)
        };
    }

    private static void render(ServerPlayerEntity player, ArrayList<ClaimEdge> edges, BlockPos pos, String color, int range) {
        Formatting colorFormatting = Formatting.byName(color);
        Vec3f rgb = new Vec3f(255, 255, 255);
        if (colorFormatting != null) rgb = new Vec3f(Vec3d.unpackRgb(colorFormatting.getColorValue()));

        ParticleEffect effect = new DustParticleEffect(rgb, 2.0F);

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
                 spawnParticleIfAllowed(player, effect, edge.projX(m), edge.projY(m), edge.projZ(m), pos, range);
            }
        }
    }

    public static void renderClaimEdges(ServerPlayerEntity player, Claim claim) {
        ArrayList<ClaimEdge> edges = claim.getClaimEdges();
        render(player, edges, claim.getPos(), claim.getColor(), 200);
    }

    public static void renderClaimForPlacement(ServerPlayerEntity player, BlockPos pos, String color) {
        ArrayList<ClaimEdge> edges = new ArrayList<>();
        BlockPos[] corners = getCorners(pos);

        int y = pos.getY();
        int minX = corners[0].getX();
        int minZ = corners[0].getZ();
        int maxX = corners[1].getX();
        int maxZ = corners[1].getZ();

        edges.add(new ClaimEdge(minX, y, minZ, minX, y, maxZ));
        edges.add(new ClaimEdge(maxX, y, minZ, maxX, y, maxZ));
        edges.add(new ClaimEdge(minX, y, minZ, maxX, y, minZ));
        edges.add(new ClaimEdge(minX, y, maxZ, maxX, y, maxZ));
        render(player, edges, pos, color, 256 * 256);
    }

    private static void spawnParticleIfAllowed(ServerPlayerEntity player, ParticleEffect effect, double x, double y, double z, BlockPos ignoreClaimPos, int range) {
        ServerWorld world = player.getServerWorld();

        if (y < 0) return;

        Vec3d delta = player.getPos().subtract(x, y, z);
        double length2 = delta.lengthSquared();
        if (length2 > range) {
            return;
        }

        Vec3d rotation = player.getRotationVec(1.0F);
        double dot = (delta.multiply(1.0 / Math.sqrt(length2))).dotProduct(rotation);
        if (dot > 0.0) {
            return;
        }

        BlockPos particlePos = new BlockPos(x, y, z);
        if (!ClaimManager.particleAllowedAt(particlePos, ignoreClaimPos)) return;

        world.spawnParticles(
                player, effect, true,
                particlePos.getX(), particlePos.getY(), particlePos.getZ(),
                1,
                0.0, 0.0, 0.0,
                0.0
        );
    }
}
