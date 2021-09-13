package arnaria.kingdoms.services.data;

import arnaria.kingdoms.services.procedures.KingdomProcedures;
import com.google.gson.JsonElement;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;

import java.util.ArrayList;
import java.util.UUID;

public class KingdomsData {

    private static final Table kingdomData = KingdomProcedures.kingdomData;

    public static ArrayList<String> getKingdomIds() {
        return (ArrayList<String>) kingdomData.getIds();
    }

    public static UUID getKing(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return kingdom.getUuid("KING");
    }

    public static String getColor(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return kingdom.getString("COLOR");
    }

    public static ArrayList<UUID> getMembers(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        ArrayList<UUID> members = new ArrayList<>();
        for (JsonElement member : kingdom.getJson("MEMBERS").getAsJsonArray()) {
            members.add(UUID.fromString(member.getAsString()));
        }
        return members;
    }

    public static ArrayList<UUID> getJoinRequests(String kingdomID) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        ArrayList<UUID> requests = new ArrayList<>();
        for (JsonElement request : kingdom.getJson("REQUESTS").getAsJsonArray()) {
            requests.add(UUID.fromString(request.getAsString()));
        }
        return requests;
    }

    public static int getClaimMarkerPointsTotal(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return kingdom.getInt("CLAIM_MARKER_POINTS_TOTAL");
    }

    public static int getClaimMarkerPointsUsed(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return kingdom.getInt("CLAIM_MARKER_POINTS_USED");
    }
}
