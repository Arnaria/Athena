package arnaria.kingdoms.services.data;

import com.google.gson.JsonElement;
import mrnavastar.sqlib.api.DataContainer;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static arnaria.kingdoms.services.procedures.KingdomProcedures.kingdomData;

public class KingdomsData {

    public static ArrayList<String> getKingdomIds() {
        return (ArrayList<String>) kingdomData.getIds();
    }

    public static UUID getKing(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return kingdom.getUuid("KING");
    }

    public static ArrayList<UUID> getAdvisers(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        ArrayList<UUID> advisers = new ArrayList<>();
        for (JsonElement adviser : kingdom.getJson("ADVISERS").getAsJsonArray()) {
            advisers.add(UUID.fromString(adviser.getAsString()));
        }
        return advisers;
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

    public static ArrayList<UUID> getBlockedPlayers(String kingdomID) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        ArrayList<UUID> blockedPlayers = new ArrayList<>();
        for (JsonElement request : kingdom.getJson("BLOCKED").getAsJsonArray()) {
            blockedPlayers.add(UUID.fromString(request.getAsString()));
        }
        return blockedPlayers;
    }

    public static int getBannerCount(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return kingdom.getInt("BANNER_COUNT");
    }

    public static BlockPos getStartingBannerPos(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return kingdom.getBlockPos("STARTING_BANNER_POS");
    }

    public static int getXp(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return kingdom.getInt("XP");
    }

    public static ArrayList<String> getChallengeQue(String kingdomId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        return (ArrayList<String>) Arrays.asList(kingdom.getStringArray("CHALLENGE_QUE"));
    }
}
