package arnaria.kingdoms.util.procedures;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;

import static arnaria.kingdoms.Kingdoms.playerManager;

public class KingdomProcedures {

    public static final Table kingdomData = new Table("KingdomData");

    public static void createKingdom(String kingdomId, UUID uuid) {
        kingdomData.beginTransaction();
        DataContainer kingdom = new DataContainer(kingdomId);
        kingdomData.put(kingdom);

        kingdom.put("KING", uuid);
        kingdom.put("COLOR", "");
        kingdom.put("MEMBERS", new JsonArray());

        addMember(kingdomId, uuid);

        PlayerEntity executor = playerManager.getPlayer(uuid);
        if (executor != null) {
            ((PlayerEntityInf) executor).setKingship(true);
        }
        kingdomData.endTransaction();
    }

    public static void disbandKingdom(String kingdomId, UUID uuid) {
        kingdomData.drop(kingdomId);

        List<ServerPlayerEntity> onlinePlayers = playerManager.getPlayerList();
        for (ServerPlayerEntity player : onlinePlayers) {
            if (((PlayerEntityInf) player).getKingdomId().equals(kingdomId)) {
                ((PlayerEntityInf) player).setKingdomId("");
                ((PlayerEntityInf) player).setKingship(false);
            }
        }
    }

    public static void setKing(String kingdomId, UUID uuid) {
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
            }
        }
    }

    public static void setKingdomColor(String kingdomId, String color) {
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
}