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

    private static final Table kingdomData = new Table("KingdomData");

    public static void createKingdom(String kingdomName, UUID uuid) {
        DataContainer kingdom = new DataContainer(kingdomName);
        kingdomData.put(kingdom);

        JsonArray members = new JsonArray();
        members.add(uuid.toString());
        kingdom.put("KING", uuid.toString());
        kingdom.put("MEMBERS", members);

        PlayerEntity executor = playerManager.getPlayer(uuid);
        if (executor != null) {
            ((PlayerEntityInf) executor).setKingdomId(kingdomName);
            ((PlayerEntityInf) executor).setKingship(true);
        }
    }

    public static void disbandKingdom(String kingdomName, UUID uuid) {
        kingdomData.drop(kingdomName);

        List<ServerPlayerEntity> onlinePlayers = playerManager.getPlayerList();
        for (ServerPlayerEntity player : onlinePlayers) {
            if (((PlayerEntityInf) player).getKingdomId().equals(kingdomName)) {
                ((PlayerEntityInf) player).setKingdomId("none");
                ((PlayerEntityInf) player).setKingship(false);
            }
        }
    }

    public static List<String> getKingdoms() {
        return kingdomData.getIds();
    }

    public static void addMember(String kingdomName, UUID uuid) {
        DataContainer kingdom = kingdomData.get(kingdomName);

        JsonArray members = kingdom.getJson("MEMBERS").getAsJsonArray();
        members.add(uuid.toString());
        kingdom.put("MEMBERS", members);
    }

    public static void removeMember(String kingdomName, UUID uuid) {
        DataContainer kingdom = kingdomData.get(kingdomName);

        JsonArray members = kingdom.getJson("MEMBERS").getAsJsonArray();

        int index = 0;
        for (JsonElement member : members) {
            if (member.getAsString().equals(uuid.toString())) members.remove(index);
            index++;
        }

        kingdom.put("MEMBERS", members);
    }

    public static JsonArray getMembers(String kingdomName) {
        DataContainer kingdom = kingdomData.get(kingdomName);
        return kingdom.getJson("MEMBERS").getAsJsonArray();
    }
}
