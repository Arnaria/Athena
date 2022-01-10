package corviolis.athena.services.api;

import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.services.procedures.KingdomProcedureChecks;
import corviolis.athena.services.procedures.KingdomProcedures;
import corviolis.athena.services.procedures.LinkingProcedures;
import corviolis.athena.util.BetterPlayerManager;
import corviolis.athena.util.InterfaceTypes;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class NationsAPI {

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
                String action = obj.get("action").getAsString();

                if (action.equals("create")) {
                    KingdomProcedureChecks.createKingdom(InterfaceTypes.API, obj.get("name").getAsString(), executor);
                }

                if (action.equals("disband")) {
                    KingdomProcedureChecks.disbandKingdom(InterfaceTypes.API, kingdomId, executor);
                }

                if (action.equals("update_color")) {
                    Formatting color = Formatting.byName(obj.get("color").getAsString());
                    if (color != null) KingdomProcedureChecks.setColour(InterfaceTypes.API, kingdomId, color, executor);
                }

                if (action.equals("kick_member")) {
                    UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                    KingdomProcedureChecks.removePlayer(InterfaceTypes.API, kingdomId, uuid, executor);
                }

                if (action.equals("banish_member")) {
                    UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                    KingdomProcedureChecks.banishPlayer(InterfaceTypes.API, kingdomId, uuid, executor);
                }

                if (action.equals("unbanish_member")) {
                    UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                    KingdomProcedureChecks.unBanishPlayer(InterfaceTypes.API, kingdomId, uuid, executor);
                }

                if (action.equals("accept_join_request")) {
                    UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                    KingdomProcedureChecks.acceptJoinRequest(InterfaceTypes.API, kingdomId, uuid, executor);
                }

                if (action.equals("decline_join_request")) {
                    UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                    KingdomProcedureChecks.declineJoinRequest(InterfaceTypes.API, kingdomId, uuid, executor);
                }

                if (action.equals("add_advisor")) {
                    UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                    KingdomProcedureChecks.addAdviser(InterfaceTypes.API, kingdomId, uuid, executor);
                }

                if (action.equals("remove_advisor")) {
                    UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                    KingdomProcedureChecks.removeAdviser(InterfaceTypes.API, kingdomId, uuid, executor);
                }

                if (action.equals("transfer_kingship")) {
                    UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
                    KingdomProcedureChecks.transferKingShip(InterfaceTypes.API, kingdomId, uuid, executor);
                }
            }
        });
    }
}