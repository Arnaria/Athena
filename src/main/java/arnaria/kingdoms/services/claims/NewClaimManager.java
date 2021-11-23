package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.Kingdoms;
import arnaria.kingdoms.interfaces.BannerMarkerInf;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.BlueMapAPI;
import arnaria.kingdoms.util.ClaimHelpers;
import arnaria.kingdoms.util.Parser;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.Shape;
import de.bluecolored.bluemap.api.marker.ShapeMarker;
import eu.pb4.holograms.api.holograms.WorldHologram;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.util.*;

import static arnaria.kingdoms.Kingdoms.log;
import static arnaria.kingdoms.Kingdoms.overworld;

public class NewClaimManager {

    private static final Table claimData = Kingdoms.database.createTable("ClaimData");
    private static final HashMap<BlockPos, ArrayList<ChunkPos>> claims = new HashMap<>();
    private static final HashMap<BlockPos, WorldHologram> holograms = new HashMap<>();
    private static final ListMultimap<String, BlockPos> points = ArrayListMultimap.create();

    public static String getKingdomId(BlockPos pos) {
        /*ListMultimap<BlockPos, String> pointsInverted = Multimaps.invertFrom(points, ArrayListMultimap.create());
        return pointsInverted.get(pos).get(0);*/

        for (Map.Entry<String, Collection<BlockPos>> positions : points.asMap().entrySet()) {
            if (positions.getValue().contains(pos)) return positions.getKey();
        }
        return "";
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

            points.put(kingdomId, pos);
        }
        claimData.endTransaction();
    }

    public static void addClaim(String kingdomId, BlockPos pos, boolean showCosmetics) {
        ArrayList<ChunkPos> chunks = ClaimHelpers.createChunkBox(pos, 5, true);
        claims.put(pos, chunks);
        points.put(kingdomId, pos);

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
        String kingdomId = getKingdomId(pos);

        holograms.get(pos).hide();
        KingdomProcedures.removeFromBannerCount(kingdomId, 1);

        Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);
        markerSet.ifPresent(markers -> markers.removeMarker(pos.toShortString()));
        BlueMapAPI.saveMarkers();

        claims.remove(pos);
        points.remove(kingdomId, pos);
        holograms.remove(pos);
        claimData.drop(pos.toShortString());
    }

    public static void dropKingdom(String kingdomId) {
        claimData.beginTransaction();
        for (BlockPos pos : points.get(kingdomId)) {
            dropClaim(pos);
        }
        claimData.endTransaction();
    }

    /*public static void updateColor(String kingdomId, String color) {
        Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);

        for (Map.Entry<BlockPos, WorldHologram> hologram : holograms.entrySet()) {
            if (hologram.getKey().equals(kingdomId)) {
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
        }
        BlueMapAPI.saveMarkers();
    }*/

    /*public static void rebrand(String kingdomId, String newKingdomId) {
        ArrayList<WorldHologram> kingdomHolograms = holograms.get(newKingdomId);
        kingdomHolograms.addAll(holograms.get(kingdomId));
        holograms.remove(kingdomId);
        holograms.put(newKingdomId, kingdomHolograms);

        updateColor(newKingdomId, KingdomsData.getColor(newKingdomId));
    }*/

    public static boolean actionAllowedAt(BlockPos pos, PlayerEntity player) {
        if (pos.getY() < 0) return true;

        for (Map.Entry<BlockPos, ArrayList<ChunkPos>> claim : claims.entrySet()) {
            if (claim.getValue().contains(new ChunkPos(pos)) && ((PlayerEntityInf) player).allowedToEditIn(getKingdomId(pos))) return true;
        }
        return false;
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
        for (Map.Entry<BlockPos, ArrayList<ChunkPos>> claim : claims.entrySet()) {
            if (claim.getValue().contains(new ChunkPos(pos))) return false;
            if (kingdomId.equals(getKingdomId(claim.getKey())) && isOverlapping(claim.getValue(), chunks)) return true;
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