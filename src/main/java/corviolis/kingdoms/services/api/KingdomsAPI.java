package corviolis.kingdoms.services.api;

import corviolis.kingdoms.services.data.KingdomsData;
import corviolis.kingdoms.services.procedures.KingdomProcedureChecks;
import corviolis.kingdoms.services.procedures.KingdomProcedures;
import corviolis.kingdoms.services.procedures.LinkingProcedures;
import corviolis.kingdoms.util.BetterPlayerManager;
import corviolis.kingdoms.util.InterfaceTypes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import net.minecraft.util.Formatting;

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

        api.post(url + "/actions", ctx -> {
            JsonObject obj = (JsonObject) JsonParser.parseString(ctx.body());
            UUID executor = LinkingProcedures.getUuid(obj.get("access_token").getAsString());
            String kingdomId = KingdomProcedures.getKingdomId(executor);

            if (executor != null && kingdomId != null) {
                if (obj.get("action").getAsString().equals("disband")) {
                    KingdomProcedureChecks.disbandKingdom(InterfaceTypes.API, kingdomId, executor);
                }

                if (obj.get("action").getAsString().equals("update_color")) {
                    Formatting color = Formatting.byName(obj.get("color").getAsString());
                    if (color != null) KingdomProcedureChecks.setColour(InterfaceTypes.API, kingdomId, color, executor);
                }

                if (obj.get("action").getAsString().equals("banish_member")) {
                    UUID member = UUID.fromString(obj.get("member_uuid").getAsString());
                    KingdomProcedureChecks.removePlayer(InterfaceTypes.API, kingdomId, member, executor);
                }

                if (obj.get("action").getAsString().equals("accept_join_request")) {
                    UUID request = UUID.fromString(obj.get("request_uuid").getAsString());
                    KingdomProcedureChecks.acceptJoinRequest(InterfaceTypes.API, kingdomId, request, executor);
                }

                if (obj.get("action").getAsString().equals("decline_join_request")) {
                    UUID request = UUID.fromString(obj.get("request_uuid").getAsString());
                    KingdomProcedureChecks.declineJoinRequest(InterfaceTypes.API, kingdomId, request, executor);
                }
            }
        });
    }
}