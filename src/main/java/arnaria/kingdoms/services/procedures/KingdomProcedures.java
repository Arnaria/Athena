package arnaria.kingdoms.services.procedures;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.InterfaceTypes;
import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;

import static arnaria.kingdoms.Kingdoms.playerManager;

public class KingdomProcedures {

    public static final Table kingdomData = new Table("KingdomData");

    public static void setupPlayer(PlayerEntity player) {
        DataContainer kingdom = getKingdom(player);
        if (kingdom != null) {
            if (kingdom.getUuid("KING").equals(player.getUuid())) ((PlayerEntityInf) player).setKingship(true);
            ((PlayerEntityInf) player).setKingdomId(kingdom.getId());
        }
    }

    public static void createKingdom(Enum<InterfaceTypes> platform, String kingdomId, UUID uuid) {
        for (String kingdom : KingdomsData.getKingdomIds()) {
            if (KingdomsData.getMembers(kingdom).contains(uuid)) {
                if (platform.equals(InterfaceTypes.COMMAND)) NotificationManager.send(uuid, "You are already in a kingdom", NotificationTypes.ERROR);
                return;
            }
        }

        DataContainer kingdom = new DataContainer(kingdomId);
        kingdomData.put(kingdom);

        kingdom.put("KING", uuid);
        kingdom.put("COLOR", "");
        kingdom.put("MEMBERS", new JsonArray());

        addMember(kingdomId, uuid);

        PlayerEntity executor = playerManager.getPlayer(uuid);
        if (executor != null) ((PlayerEntityInf) executor).setKingship(true);

        NotificationManager.send(uuid, "You are now the leader of " + kingdomId, NotificationTypes.ACHIEVEMENT);
    }

    public static void disbandKingdom(String kingdomId) {
        kingdomData.drop(kingdomId);
        List<ServerPlayerEntity> onlinePlayers = playerManager.getPlayerList();
        for (ServerPlayerEntity player : onlinePlayers) {
            if (((PlayerEntityInf) player).getKingdomId().equals(kingdomId)) {
                ((PlayerEntityInf) player).setKingdomId("");
                ((PlayerEntityInf) player).setKingship(false);
            }
        }
    }

    public static DataContainer getKingdom(PlayerEntity player) {
        for (DataContainer kingdom : kingdomData.getDataContainers()) {
            for(JsonElement member : kingdom.getJson("MEMBERS").getAsJsonArray()) {
                if (member.getAsString().equals(player.getUuidAsString())) return kingdom;
            }
        }
        return null;
    }

    public static void updateKing(String kingdomId, UUID uuid) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        kingdom.put("KING", uuid);

        List<ServerPlayerEntity> onlinePlayers = playerManager.getPlayerList();
        for (ServerPlayerEntity player : onlinePlayers) {
            if (((PlayerEntityInf) player).getKingdomId().equals(kingdomId) && ((PlayerEntityInf) player).isKing()) {
                ((PlayerEntityInf) player).setKingship(false);
            }

            if (player.getUuid().equals(uuid)) {
                addMember(kingdomId, uuid);
                ((PlayerEntityInf) player).setKingship(true);
                NotificationManager.send(uuid, "You are now the king of " + kingdomId + "!", NotificationTypes.ACHIEVEMENT);
            }
        }
    }

    public static void setColor(String kingdomId, String color) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        kingdom.put("COLOR", color);
    }

    public static void addMember(String kingdomId, UUID uuid) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        JsonArray members = kingdom.getJson("MEMBERS").getAsJsonArray();

        for (JsonElement member : members) {
            if (member.getAsString().equals(uuid.toString())) return;
        }

        members.add(uuid.toString());
        kingdom.put("MEMBERS", members);

        List<ServerPlayerEntity> onlinePlayers = playerManager.getPlayerList();
        for (ServerPlayerEntity player : onlinePlayers) {
            if (player.getUuid().equals(uuid)) ((PlayerEntityInf) player).setKingdomId(kingdomId);
        }
    }

    public static void removeMember(String kingdomId, UUID uuid) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        JsonArray members = kingdom.getJson("MEMBERS").getAsJsonArray();

        int index = 0;
        for (JsonElement member : members) {
            if (member.getAsString().equals(uuid.toString())) members.remove(index);
            index++;
        }
        kingdom.put("MEMBERS", members);
    }

    public static void addClaimMarkerPointsTotal(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        kingdom.put("CLAIM_MARKER_POINTS_TOTAL", amount);
    }

    public static void removeClaimMarkerPointsTotal(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int originalAmount = kingdom.getInt("CLAIM_MARKER_POINTS");
        kingdom.put("CLAIM_MARKER_POINTS_TOTAL", originalAmount - amount);
    }

    public static void addClaimMarkerPointsUsed(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        kingdom.put("CLAIM_MARKER_POINTS_USED", amount);
    }

    public static void removeClaimMarkerPointsUsed(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int originalAmount = kingdom.getInt("CLAIM_MARKER_POINTS_USED");
        kingdom.put("CLAIM_MARKER_POINTS_TOTAL_USED", originalAmount - amount);
    }
}