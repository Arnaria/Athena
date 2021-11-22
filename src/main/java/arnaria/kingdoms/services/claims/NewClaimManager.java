package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.Kingdoms;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.BlueMapAPI;
import arnaria.kingdoms.util.ClaimHelpers;
import arnaria.kingdoms.util.Parser;
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
    private static final HashMap<String, ArrayList<ChunkPos>> claims = new HashMap<>();
    private static final HashMap<String, ArrayList<BlockPos>> points = new HashMap<>();
    private static final HashMap<String, ArrayList<WorldHologram>> holograms = new HashMap<>();
    //private static final HashMap<String, HashMap<BlockPos, ArrayList<BlockPos>>> links = new HashMap<>();

    public static void init() {
        claimData.beginTransaction();
        for (DataContainer claimDataContainer : claimData.getDataContainers()) {
            String kingdomId = claimDataContainer.getString("KINGDOM_ID");

            ArrayList<BlockPos> kingdomPoints = new ArrayList<>();
            for (BlockPos pos : claimDataContainer.getBlockPosArray("CLAIM")) {
                kingdomPoints.add(pos);

                ArrayList<ChunkPos> chunks = claims.get(kingdomId);
                chunks.addAll(ClaimHelpers.createChunkBox(pos, 5, true));
                claims.put(kingdomId, chunks);
            }

            points.put(kingdomId, kingdomPoints);
        }
        claimData.endTransaction();
    }

    public static void addClaim(String kingdomId, BlockPos pos, boolean bluemap) {
        ArrayList<ChunkPos> chunks = claims.get(kingdomId);
        chunks.addAll(ClaimHelpers.createChunkBox(pos, 5, true));
        claims.put(kingdomId, chunks);

        ArrayList<BlockPos> kingdomPoints = points.get(kingdomId);
        kingdomPoints.add(pos);
        points.put(kingdomId, kingdomPoints);

        ArrayList<WorldHologram> kingdomHolograms = holograms.get(kingdomId);
        WorldHologram hologram = new WorldHologram(Kingdoms.overworld, new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
        kingdomHolograms.add(hologram);
        holograms.put(kingdomId, kingdomHolograms);

        LiteralText claimTag = new LiteralText(kingdomId.toUpperCase());
        Formatting formatting = Formatting.byName(KingdomsData.getColor(kingdomId));
        if (formatting != null) claimTag.formatted(formatting);
        hologram.addText(claimTag);
        hologram.show();

        KingdomProcedures.addToBannerCount(kingdomId, 1);
        if (KingdomsData.getStartingClaimPos(kingdomId) == null) KingdomProcedures.setStartingClaimPos(kingdomId, pos);

        if (bluemap) {
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
        DataContainer claimDataContainer = claimData.get(kingdomId);
        if (claimDataContainer == null) claimDataContainer = claimData.createDataContainer(kingdomId);
        claimDataContainer.put("CLAIM", kingdomPoints.toArray(BlockPos[]::new));
        claimData.endTransaction();
    }

    public static void addAdminClaim(BlockPos pos) {
        addClaim("ADMIN", pos, false);
    }

    public static void dropClaim(BlockPos pos) {
        String kingdomId = null;
        for (Map.Entry<String, ArrayList<BlockPos>> point : points.entrySet()) {
            if (point.getValue().contains(pos)) kingdomId = point.getKey();
        }

        if (kingdomId != null) {
            ArrayList<ChunkPos> chunksToDrop = ClaimHelpers.createChunkBox(pos, 5, true);
            ArrayList<ChunkPos> claim = claims.get(kingdomId);
            claim.removeAll(chunksToDrop);
            claims.put(kingdomId, claim);

            points.get(kingdomId).remove(pos);

            ArrayList<WorldHologram> kingdomHolograms = holograms.get(kingdomId);
            WorldHologram kingdomHologram = null;
            for (WorldHologram hologram : kingdomHolograms) {
                if (chunksToDrop.contains(hologram.getChunkPos())) {
                    kingdomHologram = hologram;
                    break;
                }
            }

            if (kingdomHologram != null) {
                kingdomHologram.hide();
                kingdomHolograms.remove(kingdomHologram);
            }

            KingdomProcedures.removeFromBannerCount(kingdomId, 1);

            Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);
            markerSet.ifPresent(markers -> markers.removeMarker(pos.toShortString()));
            BlueMapAPI.saveMarkers();

            claimData.drop(pos.toShortString());
        }
    }

    public static void dropKingdom(String kingdomId) {
        ArrayList<WorldHologram> kingdomHolograms = holograms.get(kingdomId);
        for (WorldHologram hologram : kingdomHolograms) hologram.hide();

        claims.remove(kingdomId);
        points.remove(kingdomId);
        holograms.remove(kingdomId);
    }

    public static void updateColor(String kingdomId, String color) {
        Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);

        for (WorldHologram hologram : holograms.get(kingdomId)) {
            hologram.removeElement(0);
            hologram.addText(new LiteralText(kingdomId.toUpperCase()).formatted(Formatting.byName(color)));

            markerSet.ifPresent(markers -> {
                BlockPos pos = new BlockPos(hologram.getPosition());
                markers.removeMarker(kingdomId + " : " + pos.toShortString());

                BlockPos[] corners = ClaimHelpers.getCorners(pos);
                ShapeMarker marker = markers.createShapeMarker(kingdomId + " : " + pos.toShortString(), BlueMapAPI.getOverworld(), corners[0].getX(), corners[0].getY(), corners[0].getZ(), Shape.createRect(corners[0].getX(), corners[0].getZ(), corners[1].getX(), corners[1].getZ()), pos.getY());
                Vec3f rgb = Parser.colorNameToRGB(KingdomsData.getColor(kingdomId));
                Color c = new Color(rgb.getX(), rgb.getY(), rgb.getZ(), 0.5F);
                marker.setColors(c, c.darker());
            });
        }
        BlueMapAPI.saveMarkers();
    }

    public static void rebrand(String kingdomId, String newKingdomId) {
        ArrayList<WorldHologram> kingdomHolograms = holograms.get(newKingdomId);
        kingdomHolograms.addAll(holograms.get(kingdomId));
        holograms.remove(kingdomId);
        holograms.put(newKingdomId, kingdomHolograms);

        updateColor(newKingdomId, KingdomsData.getColor(newKingdomId));
    }

    public static boolean actionAllowedAt(BlockPos pos, PlayerEntity player) {
        if (pos.getY() < 0) return true;

        ChunkPos chunk = new ChunkPos(pos);
        for (Map.Entry<String, ArrayList<ChunkPos>> claim : claims.entrySet()) {
            if (claim.getValue().contains(chunk) && ((PlayerEntityInf) player).getKingdomId().equals(claim.getKey())) return false;
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

    public static boolean validBannerPos(String kingdomId, BlockPos pos) {
        if (KingdomsData.getBannerCount(kingdomId) == 0) return true;

        ArrayList<ChunkPos> chunks = ClaimHelpers.createChunkBox(pos, 7, false);
        for (Map.Entry<String, ArrayList<ChunkPos>> claim : claims.entrySet()) {
            if (claim.getValue().contains(new ChunkPos(pos))) return false;
            if (claim.getKey().equals(kingdomId) && isOverlapping(claim.getValue(), chunks)) return true;
        }
        return false;
    }

    public static boolean claimExistsAt(ChunkPos pos) {
        for (ArrayList<ChunkPos> claim : claims.values()) {
            if (claim.contains(pos)) return true;
        }
        return false;
    }

    public static boolean canAffordBanner(String kingdomId) {
        int bannersAllowed = (int) Math.floor((float) KingdomsData.getXp(kingdomId) / 1000) + 1;
        return bannersAllowed > KingdomsData.getBannerCount(kingdomId);
    }
}