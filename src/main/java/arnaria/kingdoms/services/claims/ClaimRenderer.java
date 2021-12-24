package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.Parser;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3f;

import java.util.ArrayList;
import java.util.HashMap;

public class ClaimRenderer {

    private static final HashMap<String, ArrayList<Pair<BlockPos, BlockPos>>> renderMaps = new HashMap<>();
    private static final ArrayList<ArrayList<Pair<BlockPos, BlockPos>>> tempRenderMaps = new ArrayList<>();
    private static final HashMap<String, Vec3f> colors = new HashMap<>();

    private static void spawnParticle(ServerPlayerEntity player, BlockPos pos, Vec3f color, int range) {
        if (pos.getSquaredDistance(player.getBlockPos()) > range) return;

        DustParticleEffect effect = new DustParticleEffect(color, 2.0F);
        ServerWorld world = player.getWorld();
        world.spawnParticles(player, effect, true, pos.getX(), pos.getY(), pos.getZ(), 1,0, 0, 0, 0);
    }

    public static void renderClaimLayer(ServerPlayerEntity player, Vec3f color,  ArrayList<Pair<BlockPos, BlockPos>> renderMap, int y) {
        for (Pair<BlockPos, BlockPos> line : renderMap) {
            BlockPos pos = line.getLeft().add(0, y, 0);
            if (line.getLeft().getZ() == line.getRight().getZ()) {
                for (int i = 0; i <= 16; i += 2) {
                    spawnParticle(player, pos, color, 110);
                    pos = pos.add(-2, 0, 0);
                }
            } else {
                for (int i = 0; i <= 16; i += 2) {
                    spawnParticle(player, pos, color, 110);
                    pos = pos.add(0, 0, -2);
                }
            }
        }
    }

    public static void render(ServerPlayerEntity player) {
        renderMaps.forEach((kingdomId, lines) -> lines.forEach(line -> {
            for (int y = player.getBlockY() - 6; y < player.getBlockY() + 6; y++) {
                renderClaimLayer(player, colors.get(kingdomId), lines, y);
            }
        }));
    }

    public static void renderForPlacement(ServerPlayerEntity player) {
        renderMaps.forEach((kingdomId, lines) -> {
            renderClaimLayer(player, colors.get(kingdomId), lines, player.getBlockY());
        });

        tempRenderMaps.forEach(renderMap -> {
            renderClaimLayer(player, new Vec3f(255, 255, 255), renderMap, player.getBlockY());
        });
    }

    public static void updateRenderMap(String kingdomId, ArrayList<ChunkPos> chunks) {
        ArrayList<Pair<BlockPos, BlockPos>> renderMap = new ArrayList<>();
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

        renderMaps.put(kingdomId, renderMap);
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