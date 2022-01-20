package corviolis.athena.services.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import corviolis.athena.services.data.KingdomsData;
import io.javalin.Javalin;

public class LeaderboardAPI {

    private static final String url = "/v1/leaderboard";

    public static void init(Javalin api) {
        api.get(url, ctx -> {
            JsonArray kingdoms = new JsonArray();
            for (String kingdomId :  KingdomsData.getKingdomIds()) {
                JsonObject kingdom = new JsonObject();
                kingdom.addProperty("kingdomId", kingdomId);
                kingdom.addProperty("xp", KingdomsData.getXp(kingdomId));
                kingdom.addProperty("color", KingdomsData.getColor(kingdomId));
            }
            ctx.json(kingdoms.toString());
        });
    }
}
