package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.Kingdoms;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.BlueMapAPI;
import arnaria.kingdoms.util.ClaimHelpers;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
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

public class NewClaimManager {

    private static final Table claimData = Kingdoms.database.createTable("ClaimData");
    private static final HashMap<BlockPos, ArrayList<ChunkPos>> claims = new HashMap<>();
    private static final HashMap<ChunkPos, String> owners = new HashMap<>();
    private static final ListMultimap<BlockPos, BlockPos> links = ArrayListMultimap.create();
    private static final HashMap<BlockPos, WorldHologram> holograms = new HashMap<>();

    public static ArrayList<BlockPos> getBanners(String kingdomId) {
        ArrayList<BlockPos> points = new ArrayList<>();
        claims.forEach((bannerPos, chunks) -> {
            if (owners.get(new ChunkPos(bannerPos)).equals(kingdomId)) points.add(bannerPos);
        });
        return points;
    }

    //Links are currently lost on server reboot
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

    public static void addClaim(String kingdomId, BlockPos pos, boolean showCosmetics) {
        ArrayList<ChunkPos> chunks = ClaimHelpers.createChunkBox(pos, 5, true);
        ArrayList<ChunkPos> testChunks = ClaimHelpers.createChunkBox(pos, 7, false);
        claims.put(pos, chunks);

        chunks.forEach(chunk -> owners.put(chunk, kingdomId));

        ArrayList<ChunkPos> kingdomChunks = new ArrayList<>();
        for (BlockPos claim : getBanners(kingdomId)) {
            ArrayList<ChunkPos> claimChunks = claims.get(claim);
            kingdomChunks.addAll(claimChunks);

            if (!claim.equals(pos)) {
                if (isOverlapping(claimChunks, testChunks)) {
                    links.put(claim, pos);
                    links.put(pos, claim);
                }
            }
        }

        ClaimRenderer.updateRenderMap(kingdomId, kingdomChunks);
        KingdomProcedures.addToBannerCount(kingdomId, 1);
        if (KingdomsData.getStartingClaimPos(kingdomId) == null) KingdomProcedures.setStartingClaimPos(kingdomId, pos);

        if (showCosmetics) {
            WorldHologram hologram = new WorldHologram(Kingdoms.overworld, new Vec3d(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5));
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

        //New ArrayList is needed to fix concurrent modification error
        for (BlockPos claimPos : new ArrayList<>(links.get(pos))) links.remove(claimPos, pos);

        claims.get(pos).forEach(owners::remove);
        claims.remove(pos);
        links.removeAll(pos);
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

    //Consider moving into valid banner pos if not used anywhere else
    private static boolean isOverlapping(ArrayList<ChunkPos> claim, ArrayList<ChunkPos> newChunks) {
        for (ChunkPos pos : claim) {
            if (newChunks.contains(pos)) return true;
        }
        return false;
    }

    public static boolean isClaimMarker(BlockPos pos) {
        return claims.containsKey(pos);
    }

    public static boolean validBannerPos(String kingdomId, BlockPos pos) {
        if (KingdomsData.getBannerCount(kingdomId) == 0) return true;

        ArrayList<ChunkPos> testChunks = ClaimHelpers.createChunkBox(pos, 7, false);
        for (Map.Entry<BlockPos, ArrayList<ChunkPos>> claim : claims.entrySet()) {
            if (claim.getValue().contains(new ChunkPos(pos))) return false;
            if (kingdomId.equals(owners.get(new ChunkPos(claim.getKey()))) && isOverlapping(claim.getValue(), testChunks)) return true;
        }
        return false;
    }

    public static boolean claimExistsAt(ChunkPos pos) {
        return owners.get(pos) != null;
    }

    public static boolean canBreakClaim(BlockPos pos) {
        System.out.println("----------------------------");
        System.out.println("CanBreakClaim called!");

        String kingdomId = owners.get(new ChunkPos(pos));
        System.out.println("Kingdom Id : " + kingdomId);

        BlockPos startingClaimPos = KingdomsData.getStartingClaimPos(kingdomId);
        System.out.println("Starting Claim Pos : " + startingClaimPos.toShortString());

        System.out.println("Is Starting Banner : " + pos.equals(startingClaimPos));
        System.out.println("Banner Count = to 1 : " + (KingdomsData.getBannerCount(kingdomId) == 1));
        if (pos.equals(startingClaimPos) && KingdomsData.getBannerCount(kingdomId) == 1) return true;

        for (BlockPos claim : getBanners(kingdomId)) {
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("Claim : " + claim.toShortString());
            System.out.println("Links : " + links.get(claim));

            System.out.println("Contains : " + links.get(pos).contains(claim));
            System.out.println("Size : " + !(links.get(claim).size() > 1));
            System.out.println("SPos : " + !claim.equals(startingClaimPos));
            if (links.get(pos).contains(claim) && !(links.get(claim).size() > 1) && !claim.equals(startingClaimPos)) return false;
        }
        return true;
    }

    public static boolean canAffordBanner(String kingdomId) {
        int bannersAllowed = (int) Math.floor((float) KingdomsData.getXp(kingdomId) / 1000) + 1;
        return bannersAllowed > KingdomsData.getBannerCount(kingdomId);
    }
}