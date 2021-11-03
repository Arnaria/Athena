package arnaria.kingdoms.util;

import arnaria.kingdoms.Kingdoms;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.marker.MarkerAPI;
import de.bluecolored.bluemap.api.marker.MarkerSet;

import java.io.IOException;
import java.util.Optional;

public class BlueMapAPI {

    private static final MarkerAPI markerAPI = Kingdoms.markerAPI;

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
}
