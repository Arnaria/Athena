package corviolis.athena.services.claims;

import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.util.Parser;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ClaimRenderer {

    private static final HashMap<String, Set<Pair<BlockPos, BlockPos>>> renderMaps = new HashMap<>();
    //private static final ArrayList<ArrayList<Pair<BlockPos, BlockPos>>> tempRenderMaps = new ArrayList<>();
    private static final HashMap<String, Vec3f> colors = new HashMap<>();

    private static Set<Pair<BlockPos, BlockPos>> generateRenderMap(ArrayList<ChunkPos> chunks) {
        Set<Pair<BlockPos, BlockPos>> renderMap = new HashSet<>();
        chunks.forEach(chunk -> {
            if (!chunks.contains(new ChunkPos(chunk.x, chunk.z - 1))) {
                BlockPos pos = chunk.getStartPos();
                renderMap.add(new Pair<>(pos.add(16, 0, 0), pos));
            }

            if (!chunks.contains(new ChunkPos(chunk.x, chunk.z + 1))) {
                BlockPos pos = chunk.getStartPos();
                renderMap.add(new Pair<>(pos.add(16, 0, 16), pos.add(0, 0, 16)));
            }

            if (!chunks.contains(new ChunkPos(chunk.x - 1, chunk.z))) {
                BlockPos pos = chunk.getStartPos();
                renderMap.add(new Pair<>(pos.add(0, 0, 16), pos));
            }

            if (!chunks.contains(new ChunkPos(chunk.x + 1, chunk.z))) {
                BlockPos pos = chunk.getStartPos();
                renderMap.add(new Pair<>(pos.add(16, 0, 16), pos.add(16, 0, 0)));
            }
        });
        return renderMap;
    }

    private static void spawnParticle(ServerPlayerEntity player, BlockPos pos, Vec3f color, int range) {
        if (pos.getSquaredDistance(player.getBlockPos()) > range) return;

        DustParticleEffect effect = new DustParticleEffect(color, 2.0F);
        ServerWorld world = player.getWorld();
        world.spawnParticles(player, effect, true, pos.getX(), pos.getY(), pos.getZ(), 1,0, 0, 0, 0);
    }

    private static void renderClaimLayer(ServerPlayerEntity player, Vec3f color,  Set<Pair<BlockPos, BlockPos>> renderMap, int y, int range) {
        for (Pair<BlockPos, BlockPos> line : renderMap) {
            BlockPos pos = line.getLeft().add(0, y, 0);
            if (line.getLeft().getZ() == line.getRight().getZ()) {
                for (int i = 0; i <= 16; i += 2) {
                    spawnParticle(player, pos, color, range);
                    pos = pos.add(-2, 0, 0);
                }
            } else {
                for (int i = 0; i <= 16; i += 2) {
                    spawnParticle(player, pos, color, range);
                    pos = pos.add(0, 0, -2);
                }
            }
        }
    }

    public static void render(ServerPlayerEntity player) {
        renderMaps.forEach((kingdomId, lines) -> {
            for (int y = player.getBlockY() - 10; y < player.getBlockY() + 10; y++) {
                renderClaimLayer(player, colors.get(kingdomId), lines, y, 110);
            }
        });
    }

    public static void renderForPlacement(ServerPlayerEntity player) {
        if (player == null) return;
        String kingdomId = ((PlayerEntityInf) player).getKingdomId();
        if (kingdomId.isEmpty()) return;
        renderMaps.forEach((kingdom, lines) -> renderClaimLayer(player, colors.get(kingdom), lines, player.getBlockY(), 256 * 256));
        if (ClaimManager.validBannerPos(kingdomId, player.getBlockPos())) {
            renderClaimLayer(player, new Vec3f(255, 255, 255), generateRenderMap(ClaimHelpers.createChunkBox(player.getBlockPos(), 5, true)), player.getBlockY(), 256 * 256);
        }
    }

    public static void updateRenderMap(String kingdomId, ArrayList<ChunkPos> chunks) {
        renderMaps.put(kingdomId, generateRenderMap(chunks));
        updateColor(kingdomId, KingdomsData.getColor(kingdomId));
    }

    public static void updateColor(String kingdomId, String color) {
        colors.put(kingdomId, Parser.colorNameToRGB(color));
    }

    public static void dropRenderMap(String kingdomId) {
        renderMaps.remove(kingdomId);
        colors.remove(kingdomId);
    }
}