package arnaria.kingdoms.util;

import arnaria.kingdoms.services.claims.Claim;
import arnaria.kingdoms.services.claims.ClaimManager;
import net.minecraft.block.Blocks;
import net.minecraft.particle.DustParticleEffect;
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
        for (int i = 0; i < diameter; i++) {
            int currentZ = startZ;
            for (int j = 0; j < diameter; j++) {
                BlockPos pos = new BlockPos(currentX, 1, currentZ);
                if (removeOverlapping && !ClaimManager.claimExistsAt(pos)) chunkList.add(overworld.getChunk(pos));
                if (!removeOverlapping) chunkList.add(overworld.getChunk(pos));
                currentZ += 16;
            }
            currentX += 16;
        }
        return chunkList;
    }

    public static BlockPos[] getCorners(BlockPos center) {
        return new BlockPos[] {
            overworld.getChunk(center.add(-32, 0, -32)).getPos().getStartPos().add(-1, 0, -1),
            overworld.getChunk(center.add(32, 0, 32)).getPos().getStartPos().add(17, 0, 17)
        };
    }

    public static void spawnParticle(ServerPlayerEntity player, BlockPos pos, double offset, Formatting color, int range) {
        if (ClaimManager.claimExistsAt(pos)) return;
        if (pos.getSquaredDistance(player.getBlockPos()) > range) return;

        Vec3f rgb = new Vec3f(255, 255, 255);
        if (color != null) rgb = new Vec3f(Vec3d.unpackRgb(color.getColorValue()));
        DustParticleEffect effect = new DustParticleEffect(rgb, 2.0F);
        ServerWorld world = player.getServerWorld();

        world.spawnParticles(player, effect, true, pos.getX() + offset, pos.getY(), pos.getZ() + offset, 1,0, 0, 0, 0);
    }

    public static void renderClaimLayer(ServerPlayerEntity player, BlockPos bannerPos, int y, Formatting color, int range) {
        if (bannerPos.getSquaredDistance(player.getBlockPos()) > 256 * 256) return;
        if (player.getBlockY() < 0) return;
        BlockPos[] corners = getCorners(bannerPos);

        for (int j = 0; j < 82; j+= 2) {
            spawnParticle(player, corners[0].add(j, y, 0), 1, color, range);
            spawnParticle(player, corners[0].add(0, y, j), 1, color, range);
            spawnParticle(player, corners[1].add(-j, y, 0), -1, color, range);
            spawnParticle(player, corners[1].add(0, y, -j), -1, color, range);
        }
    }

    public static void renderClaim(ServerPlayerEntity player, Claim claim) {
        if (claim.getPos().getSquaredDistance(player.getBlockPos()) > 64 * 64) return;

        int y = player.getBlockY();
        if (y % 2 == 0) y += 1;
        for (int i = y - 8; i < y + 8; i += 2) {
            renderClaimLayer(player, claim.getPos(), i, Formatting.byName(claim.getColor()), 80);
        }
    }
}