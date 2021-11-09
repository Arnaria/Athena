package arnaria.kingdoms.services.api;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.BetterPlayerManager;
import com.google.gson.JsonObject;
import io.javalin.Javalin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class KingdomsAPI {

    private static final String url = "/v1/kingdoms";

    public static void init(Javalin api) {
        api.get(url + "/", ctx -> ctx.json(KingdomsData.getKingdomIds()));

        api.get(url +"/{kingdomId}", ctx -> {
            String kingdomId = ctx.pathParam("kingdomId");
            HashMap<String, Object> kingdom = new HashMap<>();

            kingdom.put("king_uuid", KingdomsData.getKing(kingdomId));
            kingdom.put("color", KingdomsData.getColor(kingdomId));
            kingdom.put("xp", KingdomsData.getXp(kingdomId));
            kingdom.put("banner_count", KingdomsData.getBannerCount(kingdomId));
            kingdom.put("starting_banner_pos", KingdomsData.getStartingBannerPos(kingdomId));
            ctx.json(kingdom);
        });

        api.get(url + "/{kingdomId}/members", ctx -> {
            String kingdomId = ctx.pathParam("kingdomId");
            ArrayList<HashMap<String, Object>> members = new ArrayList<>();

            for (UUID member : KingdomsData.getMembers(kingdomId)) {
                HashMap<String, Object> memberData = new HashMap<>();
                memberData.put("username", BetterPlayerManager.getName(member));
                memberData.put("uuid", member);
                members.add(memberData);
            }
            ctx.json(members);
        });

        api.get(url + "/{kingdomId}/requests", ctx -> {
            String kingdomId = ctx.pathParam("kingdomId");
            ArrayList<HashMap<String, Object>> requests = new ArrayList<>();

            for (UUID request : KingdomsData.getJoinRequests(kingdomId)) {
                HashMap<String, Object> requestData = new HashMap<>();
                requestData.put("username", BetterPlayerManager.getPlayer(request));
                requestData.put("uuid", request);
                requests.add(requestData);
            }
            ctx.json(requests);
        });

        api.get(url + "/{kingdomId}/blocked", ctx -> {
            String kingdomId = ctx.pathParam("kingdomId");
            ArrayList<HashMap<String, Object>> blocks = new ArrayList<>();

            for (UUID blocked : KingdomsData.getBlockedPlayers(kingdomId)) {
                HashMap<String, Object> blockedData = new HashMap<>();
                blockedData.put("username", BetterPlayerManager.getPlayer(blocked));
                blockedData.put("uuid", blocked);
                blocks.add(blockedData);
            }
            ctx.json(blocks);
        });

        api.post("/{kingdomId}/members", ctx -> {
            JsonObject jsonObject = ctx.bodyAsClass(JsonObject.class);
            ctx.result(jsonObject.toString());
        });
    }
}