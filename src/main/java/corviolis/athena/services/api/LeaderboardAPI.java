package corviolis.athena.services.api;

import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.util.XpComparator;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class LeaderboardAPI {

    private static final String url = "/v1/leaderboard";

    public static void init(Javalin api) {
        api.get(url, ctx -> {
            ArrayList<HashMap<String, Object>> kingdoms = new ArrayList<>();
            for (String kingdomId :  KingdomsData.getKingdomIds()) {
                if (!Objects.equals(kingdomId, "ADMIN")) {
                    HashMap<String, Object> kingdom = new HashMap<>();
                    kingdom.put("kingdomId", kingdomId);
                    kingdom.put("xp", KingdomsData.getXp(kingdomId));
                    kingdom.put("color", KingdomsData.getColor(kingdomId));
                    kingdoms.add(kingdom);
                }
            }

            kingdoms.sort(new XpComparator());
            ctx.json(kingdoms);
        });
    }
}