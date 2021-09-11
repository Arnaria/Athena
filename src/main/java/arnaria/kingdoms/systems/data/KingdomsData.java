package arnaria.kingdoms.systems.data;

import arnaria.kingdoms.systems.procedures.KingdomProcedures;
import com.google.gson.JsonElement;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;

import java.util.ArrayList;
import java.util.UUID;

public class KingdomsData {

    private static final Table kingdomData = KingdomProcedures.kingdomData;

    public static ArrayList<String> getKingdoms() {
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

    public static ArrayList<String> getMembers(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        ArrayList<String> members = new ArrayList<>();
        for (JsonElement member : kingdom.getJson("MEMBERS").getAsJsonArray()) {
            members.add(member.getAsString());
        }
        return members;
    }

    public static int getClaimMarkerPoints(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return kingdom.getInt("CLAIM_MARKER_POINTS");
    }
}
