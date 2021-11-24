package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.Kingdoms;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.BlueMapAPI;
import arnaria.kingdoms.util.ClaimHelpers;
import arnaria.kingdoms.util.Parser;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.Shape;
import de.bluecolored.bluemap.api.marker.ShapeMarker;
import eu.pb4.holograms.api.holograms.WorldHologram;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.awt.*;
import java.util.*;

public class NewClaimManager {

    private static final Table claimData = Kingdoms.database.createTable("ClaimData");
    private static final HashMap<BlockPos, ArrayList<ChunkPos>> claims = new HashMap<>();
    private static final HashMap<BlockPos, String> owners = new HashMap<>();
    private static final ListMultimap<BlockPos, BlockPos> links = ArrayListMultimap.create();
    private static final HashMap<BlockPos, WorldHologram> holograms = new HashMap<>();

    public static ArrayList<BlockPos> getPoints(String kingdomId) {
        ArrayList<BlockPos> points = new ArrayList<>();
        owners.forEach((pos, kingdom) -> {
            if (kingdom.equals(kingdomId)) points.add(pos);
        });
        return points;
    }

    public static void init() {
        ClaimEvents.register();

        claimData.beginTransaction();
        for (DataContainer claimDataContainer : claimData.getDataContainers()) {
            String kingdomId = claimDataContainer.getString("KINGDOM_ID");
            BlockPos pos = claimDataContainer.getBlockPos("POS");

            ArrayList<ChunkPos> chunks = new ArrayList<>();
            for (BlockPos chunk : claimDataContainer.getBlockPosArray("CHUNKS")) chunks.add(new ChunkPos(chunk));
            claims.put(pos, chunks);

            WorldHologram hologram = new WorldHologram(Kingdoms.overworld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
            holograms.put(pos, hologram);

            LiteralText claimTag = new LiteralText(kingdomId.toUpperCase());
            Formatting formatting = Formatting.byName(KingdomsData.getColor(kingdomId));
            if (formatting != null) claimTag.formatted(formatting);
            hologram.addText(claimTag);
            hologram.show();

            owners.put(pos, kingdomId);
        }
        claimData.endTransaction();
    }

    public static void addClaim(String kingdomId, BlockPos pos, boolean showCosmetics) {
        ArrayList<ChunkPos> chunks = ClaimHelpers.createChunkBox(pos, 5, true);
        ArrayList<ChunkPos> testChunks = ClaimHelpers.createChunkBox(pos, 7, false);
        claims.put(pos, chunks);
        owners.put(pos, kingdomId);

        claims.forEach((claimPos, claimChunks) -> {
            if (isOverlapping(claimChunks, testChunks) && owners.get(claimPos).equals(kingdomId)) {
                links.put(pos, claimPos);
                links.put(claimPos, pos);
            }
        });

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

            Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);
            markerSet.ifPresent(markers -> {
                BlockPos[] corners = ClaimHelpers.getCorners(pos);
                ShapeMarker marker = markers.createShapeMarker(pos.toShortString(), BlueMapAPI.getOverworld(), corners[0].getX(), corners[0].getY(), corners[0].getZ(), Shape.createRect(corners[0].getX(), corners[0].getZ(), corners[1].getX(), corners[1].getZ()), pos.getY());
                Vec3f rgb = Parser.colorNameToRGB(KingdomsData.getColor(kingdomId));
                Color color = new Color(rgb.getX(), rgb.getY(), rgb.getZ(), 0.5F);
                marker.setColors(color, color.darker());
                BlueMapAPI.saveMarkers();
            });
        }

        claimData.beginTransaction();
        DataContainer claimDataContainer = claimData.createDataContainer(pos.toShortString());
        claimDataContainer.put("KINGDOM_ID", kingdomId);
        claimDataContainer.put("POS", pos);

        ArrayList<BlockPos> chunkPositions = new ArrayList<>();
        chunks.iterator().forEachRemaining(chunk -> chunkPositions.add(chunk.getStartPos()));
        claimDataContainer.put("CHUNKS", chunkPositions.toArray(BlockPos[]::new));

        claimData.endTransaction();
    }

    public static void addAdminClaim(BlockPos pos) {
        addClaim("ADMIN", pos, false);
    }

    public static void dropClaim(BlockPos pos) {
        String kingdomId = owners.get(pos);

        holograms.get(pos).hide();
        KingdomProcedures.removeFromBannerCount(kingdomId, 1);

        Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);
        markerSet.ifPresent(markers -> markers.removeMarker(pos.toShortString()));
        BlueMapAPI.saveMarkers();

        for (BlockPos claimPos : links.get(pos)) links.remove(claimPos, pos);

        claims.remove(pos);
        owners.remove(pos);
        links.removeAll(pos);
        holograms.remove(pos);
        claimData.drop(pos.toShortString());
    }

    public static void dropKingdom(String kingdomId) {
        claimData.beginTransaction();
        for (BlockPos pos : getPoints(kingdomId)) {
            dropClaim(pos);
        }
        claimData.endTransaction();
    }

    public static void updateColor(String kingdomId, String color) {
        Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);

        holograms.forEach((pos, hologram) -> {
            if (owners.get(pos).equals(kingdomId)) {
                hologram.removeElement(0);
                hologram.addText(new LiteralText(kingdomId.toUpperCase()).formatted(Formatting.byName(color)));

                markerSet.ifPresent(markers -> {
                    markers.removeMarker(pos.toShortString());

                    BlockPos[] corners = ClaimHelpers.getCorners(pos);
                    ShapeMarker marker = markers.createShapeMarker(pos.toShortString(), BlueMapAPI.getOverworld(), corners[0].getX(), corners[0].getY(), corners[0].getZ(), Shape.createRect(corners[0].getX(), corners[0].getZ(), corners[1].getX(), corners[1].getZ()), pos.getY());
                    Vec3f rgb = Parser.colorNameToRGB(KingdomsData.getColor(kingdomId));
                    Color c = new Color(rgb.getX(), rgb.getY(), rgb.getZ(), 0.5F);
                    marker.setColors(c, c.darker());
                });
            }
        });
        BlueMapAPI.saveMarkers();
    }

    public static void transferClaims(String kingdomId, String newKingdomId) {
        getPoints(kingdomId).forEach(pos -> owners.put(pos, newKingdomId));
        updateColor(newKingdomId, KingdomsData.getColor(newKingdomId));
    }

    public static boolean actionAllowedAt(BlockPos pos, PlayerEntity player) {
        if (pos.getY() < 0) return true;

        for (Map.Entry<BlockPos, ArrayList<ChunkPos>> claim : claims.entrySet()) {
        if (claim.getValue().contains(new ChunkPos(pos)) && !((PlayerEntityInf) player).allowedToEditIn(owners.get(claim.getKey()))) return false;
        }
        return true;
    }

    //Consider moving into valid banner pos if not used anywhere else
    private static boolean isOverlapping(ArrayList<ChunkPos> claim, ArrayList<ChunkPos> newChunks) {
        for (ChunkPos pos : newChunks) {
            if (claim.contains(pos)) return true;
        }
        return false;
    }

    public static boolean isClaimMarker(BlockPos pos) {
        return owners.containsKey(pos);
    }

    public static boolean validBannerPos(String kingdomId, BlockPos pos) {
        if (KingdomsData.getBannerCount(kingdomId) == 0) return true;

        ArrayList<ChunkPos> testChunks = ClaimHelpers.createChunkBox(pos, 7, false);
        for (ArrayList<ChunkPos> claim : claims.values()) {
            if (claim.contains(new ChunkPos(pos))) return false;
            if (kingdomId.equals(owners.get(pos)) && isOverlapping(claim, testChunks)) return true;
        }
        return false;
    }

    public static boolean claimExistsAt(ChunkPos pos) {
        for (ArrayList<ChunkPos> claim : claims.values()) {
            if (claim.contains(pos)) return true;
        }
        return false;
    }

    public static boolean canBreakClaim(BlockPos pos) {
        String kingdomId = owners.get(pos);
        BlockPos startingClaimPos = KingdomsData.getStartingClaimPos(kingdomId);
        if (pos.equals(startingClaimPos) && KingdomsData.getBannerCount(kingdomId) == 1) return true;

        for (BlockPos claimPos : links.get(pos)) {
            if (!(links.get(claimPos).size() > 1)) return false;
        }
        return true;
    }

    public static boolean canAffordBanner(String kingdomId) {
        int bannersAllowed = (int) Math.floor((float) KingdomsData.getXp(kingdomId) / 1000) + 1;
        return bannersAllowed > KingdomsData.getBannerCount(kingdomId);
    }
}