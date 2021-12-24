package arnaria.kingdoms.util;

import arnaria.kingdoms.Kingdoms;
import arnaria.kingdoms.services.data.KingdomsData;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.Shape;
import de.bluecolored.bluemap.api.marker.ShapeMarker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class BlueMapAPI {

    private static MarkerAPI markerAPI;

    public static void init(MarkerAPI api) {
        markerAPI = api;
        ArrayList<String> kingdomIds = KingdomsData.getKingdomIds();
        for (MarkerSet set : markerAPI.getMarkerSets()) {
            if (!kingdomIds.contains(set.getId())) markerAPI.removeMarkerSet(set);
        }
        saveMarkers();
    }

    public static BlueMapMap getOverworld() {
        Optional<BlueMapMap> map = Kingdoms.blueMapAPI.getMap("world");
        return map.orElse(null);
    }

    public static MarkerAPI getMarkerApi() {
        return markerAPI;
    }

    public static Optional<MarkerSet> getMarkerSet(String kingdomId) {
        return markerAPI.getMarkerSet(kingdomId);
    }

    public static void saveMarkers() {
        try {
            markerAPI.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createMarker(String kingdomId, ArrayList<ChunkPos> chunks) {
        Optional<MarkerSet> set = markerAPI.getMarkerSet(kingdomId);
        if (set.isPresent()) {
            Vec3f rgb = Parser.colorNameToRGB(KingdomsData.getColor(kingdomId));
            Color color = new Color(rgb.getX(), rgb.getY(), rgb.getZ(), 0.5F);

            for (ChunkPos chunk : chunks) {
                BlockPos pos1 = chunk.getStartPos();
                BlockPos pos2 = pos1.add(16, 0, 16);
                ShapeMarker marker = set.get().createShapeMarker(chunk.toString(), BlueMapAPI.getOverworld(), pos1.getX(), pos1.getY(), pos1.getZ(), Shape.createRect(pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ()), 62);
                marker.setColors(color, color);
            }
            saveMarkers();
        } else {
            markerAPI.createMarkerSet(kingdomId);
            createMarker(kingdomId, chunks);
        }
    }

    public static void removeMarker(String kingdomId, ArrayList<ChunkPos> chunks) {
        Optional<MarkerSet> set = markerAPI.getMarkerSet(kingdomId);
        set.ifPresent(markerSet -> {
            for (ChunkPos chunk : chunks) {
                markerSet.removeMarker(chunk.toString());
            }
            saveMarkers();
        });
    }
}
