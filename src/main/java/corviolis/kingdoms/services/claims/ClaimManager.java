package corviolis.kingdoms.services.claims;

import corviolis.kingdoms.Kingdoms;
import corviolis.kingdoms.interfaces.PlayerEntityInf;
import corviolis.kingdoms.services.data.KingdomsData;
import corviolis.kingdoms.services.procedures.KingdomProcedures;
import corviolis.kingdoms.services.api.BlueMapAPI;
import eu.pb4.holograms.api.holograms.WorldHologram;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class ClaimManager {

    private static final Table claimData = Kingdoms.database.createTable("ClaimData");
    private static final HashMap<BlockPos, ArrayList<ChunkPos>> claims = new HashMap<>();
    private static final HashMap<ChunkPos, String> owners = new HashMap<>();
    private static final HashMap<BlockPos, WorldHologram> holograms = new HashMap<>();

    private static ArrayList<BlockPos> getBanners(String kingdomId) {
        ArrayList<BlockPos> points = new ArrayList<>();
        claims.forEach((bannerPos, chunks) -> {
            if (owners.get(new ChunkPos(bannerPos)).equals(kingdomId)) points.add(bannerPos);
        });
        return points;
    }

    private static boolean isOverlapping(ArrayList<ChunkPos> claim, ArrayList<ChunkPos> newChunks) {
        for (ChunkPos pos : claim) {
            if (newChunks.contains(pos)) return true;
        }
        return false;
    }

    private static void magicScan(BlockPos pos, ArrayList<BlockPos> plsScanMe, ArrayList<BlockPos> scanned) {
        scanned.add(pos);

        ArrayList<BlockPos> overlapping = new ArrayList<>();
        ArrayList<ChunkPos> testChunks = ClaimHelpers.createChunkBox(pos, 7, false);
        for (BlockPos banner : plsScanMe) {
            if (isOverlapping(claims.get(banner), testChunks)) overlapping.add(banner);
        }

        for (BlockPos pos1 : overlapping) {
            if (!scanned.contains(pos1)) magicScan(pos1, plsScanMe, scanned);
        }
    }

    public static void init() {
        ClaimEvents.register();

        for (DataContainer claim : claimData.getDataContainers()) {
            String kingdomId = claim.getString("KINGDOM_ID");
            BlockPos pos = claim.getBlockPos("POS");

            ArrayList<ChunkPos> chunks = new ArrayList<>();
            for (BlockPos chunk : claim.getBlockPosArray("CHUNKS")) {
                ChunkPos chunkPos = new ChunkPos(chunk);
                chunks.add(chunkPos);
                owners.put(chunkPos, kingdomId);
            }
            claims.put(pos, chunks);

            WorldHologram hologram = new WorldHologram(Kingdoms.overworld, new Vec3d(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5));
            holograms.put(pos, hologram);

            LiteralText claimTag = new LiteralText(kingdomId.toUpperCase());
            Formatting formatting = Formatting.byName(KingdomsData.getColor(kingdomId));
            if (formatting != null) claimTag.formatted(formatting);
            hologram.addText(claimTag);
            hologram.show();
        }
    }

    public static void addClaim(String kingdomId, BlockPos pos, boolean showCosmetics, int labelOffset) {
        ArrayList<ChunkPos> chunks = ClaimHelpers.createChunkBox(pos, 5, true);
        claims.put(pos, chunks);

        chunks.forEach(chunk -> owners.put(chunk, kingdomId));

        ArrayList<ChunkPos> kingdomChunks = new ArrayList<>();
        for (BlockPos claim : getBanners(kingdomId)) kingdomChunks.addAll(claims.get(claim));

        ClaimRenderer.updateRenderMap(kingdomId, kingdomChunks);
        KingdomProcedures.addToBannerCount(kingdomId, 1);
        if (KingdomsData.getStartingClaimPos(kingdomId) == null) KingdomProcedures.setStartingClaimPos(kingdomId, pos);

        if (showCosmetics) {
            WorldHologram hologram = new WorldHologram(Kingdoms.overworld, new Vec3d(pos.getX() + 0.5, pos.getY() + labelOffset, pos.getZ() + 0.5));
            holograms.put(pos, hologram);

            LiteralText claimTag = new LiteralText(kingdomId.toUpperCase());
            Formatting formatting = Formatting.byName(KingdomsData.getColor(kingdomId));
            if (formatting != null) claimTag.formatted(formatting);
            hologram.addText(claimTag);
            hologram.show();

            BlueMapAPI.createMarker(kingdomId, chunks);
        }

        DataContainer claimDataContainer = claimData.createDataContainer(pos.toShortString());
        claimDataContainer.put("KINGDOM_ID", kingdomId);
        claimDataContainer.put("POS", pos);

        ArrayList<BlockPos> chunkPositions = new ArrayList<>();
        chunks.iterator().forEachRemaining(chunk -> chunkPositions.add(chunk.getStartPos()));
        claimDataContainer.put("CHUNKS", chunkPositions.toArray(BlockPos[]::new));
    }

    public static void addAdminClaim(ChunkPos pos) {
        ArrayList<BlockPos> point = getBanners("ADMIN");
        if (point.isEmpty()) {
            point.add(pos.getStartPos());
            owners.put(pos, "ADMIN");
        }
        BlockPos claimPos = point.get(0);

        ArrayList<ChunkPos> chunks = claims.get(claimPos);
        if (chunks == null) chunks = new ArrayList<>();
        chunks.add(pos);
        claims.put(claimPos, chunks);

        ClaimRenderer.updateRenderMap("ADMIN", chunks);

        DataContainer claim = claimData.createDataContainer(claimPos.toShortString());
        claim.put("KINGDOM_ID", "ADMIN");
        claim.put("POS", claimPos);

        ArrayList<BlockPos> chunkPositions = new ArrayList<>();
        chunks.iterator().forEachRemaining(chunk -> chunkPositions.add(chunk.getStartPos()));
        claim.put("CHUNKS", chunkPositions.toArray(BlockPos[]::new));
    }

    public static void dropClaim(BlockPos pos) {
        String kingdomId = owners.get(new ChunkPos(pos));

        holograms.get(pos).hide();
        KingdomProcedures.removeFromBannerCount(kingdomId, 1);
        BlueMapAPI.removeMarker(kingdomId, claims.get(pos));

        claims.get(pos).forEach(owners::remove);
        claims.remove(pos);
        holograms.remove(pos);
        claimData.drop(pos.toShortString());

        ArrayList<ChunkPos> kingdomChunks = new ArrayList<>();
        claims.forEach((claimPos, claimChunks) -> {
            if (owners.get(new ChunkPos(claimPos)).equals(kingdomId)) kingdomChunks.addAll(claimChunks);
        });
        ClaimRenderer.updateRenderMap(kingdomId, kingdomChunks);
    }

    public static void dropKingdom(String kingdomId) {
        for (BlockPos pos : getBanners(kingdomId)) {
            dropClaim(pos);
        }
        ClaimRenderer.dropRenderMap(kingdomId);
    }

    public static void dropChunk(ChunkPos pos) {
        for (Map.Entry<BlockPos, ArrayList<ChunkPos>> claim : claims.entrySet()) {
            ArrayList<ChunkPos> chunks = claim.getValue();
            if (chunks.contains(pos)) {
                chunks.remove(pos);
                owners.remove(pos);

                if (chunks.isEmpty()) claims.remove(claim.getKey());

                String kingdomId = owners.get(new ChunkPos(claim.getKey()));
                ClaimRenderer.updateRenderMap(kingdomId, chunks);
                BlueMapAPI.removeMarker(kingdomId, claims.get(claim.getKey()));
                BlueMapAPI.createMarker(kingdomId, chunks);

                DataContainer claimDataContainer = claimData.get(claim.getKey().toShortString());
                ArrayList<BlockPos> chunkPositions = new ArrayList<>(Arrays.asList(claimDataContainer.getBlockPosArray("CHUNKS")));
                chunkPositions.remove(pos.getStartPos());
                claimDataContainer.put("CHUNKS", chunkPositions.toArray(BlockPos[]::new));
                break;
            }
        }
    }

    public static void updateColor(String kingdomId, String color) {
        holograms.forEach((pos, hologram) -> {
            if (owners.get(new ChunkPos(pos)).equals(kingdomId)) {
                hologram.removeElement(0);
                hologram.addText(new LiteralText(kingdomId.toUpperCase()).formatted(Formatting.byName(color)));

                ArrayList<ChunkPos> chunks = claims.get(pos);
                BlueMapAPI.removeMarker(kingdomId, chunks);
                BlueMapAPI.createMarker(kingdomId, chunks);
            }
        });

        ClaimRenderer.updateColor(kingdomId, color);
    }

    public static void transferClaims(String kingdomId, String newKingdomId) {
        getBanners(kingdomId).forEach(pos -> owners.put(new ChunkPos(pos), newKingdomId));
        updateColor(newKingdomId, KingdomsData.getColor(newKingdomId));
    }

    public static boolean actionAllowedAt(BlockPos pos, PlayerEntity player) {
        if (pos.getY() < 0) return true;
        String kingdomId = owners.get(new ChunkPos(pos));
        if (kingdomId == null) return true;
        else return ((PlayerEntityInf) player).allowedToEditIn(kingdomId);
    }

    public static boolean isClaimMarker(BlockPos pos) {
        return claims.containsKey(pos);
    }

    public static boolean validBannerPos(String kingdomId, BlockPos pos) {
        if (KingdomsData.getBannerCount(kingdomId) == 0) return true;
        if (claimExistsAt(new ChunkPos(pos))) return false;

        ArrayList<ChunkPos> testChunks = ClaimHelpers.createChunkBox(pos, 7, false);
        for (Map.Entry<BlockPos, ArrayList<ChunkPos>> claim : claims.entrySet()) {
            if (kingdomId.equals(owners.get(new ChunkPos(claim.getKey()))) && isOverlapping(claim.getValue(), testChunks)) return true;
        }
        return false;
    }

    public static boolean claimExistsAt(ChunkPos pos) {
        return owners.get(pos) != null;
    }

    public static boolean canBreakClaim(BlockPos pos) {
        ArrayList<BlockPos> scanned = new ArrayList<>();
        ArrayList<BlockPos> plsScanMe = getBanners(owners.get(new ChunkPos(pos)));
        plsScanMe.remove(pos);

        if (plsScanMe.size() == 0) return true;
        magicScan(plsScanMe.get(0), plsScanMe, scanned);

        return scanned.size() >= plsScanMe.size();
    }

    public static boolean canAffordBanner(String kingdomId) {
        int bannersAllowed = (int) Math.floor((float) KingdomsData.getXp(kingdomId) / 1000) + 1;
        return bannersAllowed > KingdomsData.getBannerCount(kingdomId);
    }
}