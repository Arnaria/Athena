package corviolis.athena.services.data;

import com.google.gson.JsonElement;
import corviolis.athena.services.procedures.KingdomProcedures;
import mrnavastar.sqlib.api.DataContainer;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class KingdomsData {

    public static ArrayList<String> getKingdomIds() {
        ArrayList<String> kingdomIds = (ArrayList<String>) KingdomProcedures.kingdomData.getIds();
        kingdomIds.remove("ADMIN");
        return kingdomIds;
    }

    public static UUID getKing(String kingdomId) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomId);
        return kingdom.getUuid("KING");
    }

    public static ArrayList<UUID> getAdvisers(String kingdomId) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomId);
        ArrayList<UUID> advisers = new ArrayList<>();
        for (JsonElement adviser : kingdom.getJson("ADVISERS").getAsJsonArray()) {
            advisers.add(UUID.fromString(adviser.getAsString()));
        }
        return advisers;
    }

    public static String getColor(String kingdomId) {
        if (kingdomId.equals("ADMIN")) return "white";
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomId);
        return kingdom.getString("COLOR");
    }

    public static ArrayList<UUID> getMembers(String kingdomId) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomId);
        ArrayList<UUID> members = new ArrayList<>();
        for (JsonElement member : kingdom.getJson("MEMBERS").getAsJsonArray()) {
            members.add(UUID.fromString(member.getAsString()));
        }
        return members;
    }

    public static ArrayList<UUID> getJoinRequests(String kingdomID) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomID);
        ArrayList<UUID> requests = new ArrayList<>();
        for (JsonElement request : kingdom.getJson("REQUESTS").getAsJsonArray()) {
            requests.add(UUID.fromString(request.getAsString()));
        }
        return requests;
    }

    public static ArrayList<UUID> getBlockedPlayers(String kingdomID) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomID);
        ArrayList<UUID> blockedPlayers = new ArrayList<>();
        for (JsonElement request : kingdom.getJson("BLOCKED").getAsJsonArray()) {
            blockedPlayers.add(UUID.fromString(request.getAsString()));
        }
        return blockedPlayers;
    }

    public static int getBannerCount(String kingdomId) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomId);
        return kingdom.getInt("BANNER_COUNT");
    }

    public static int getXp(String kingdomId) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomId);
        return kingdom.getInt("XP");
    }

    public static BlockPos getStartingClaimPos(String kingdomId) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomId);
        return kingdom.getBlockPos("STARTING_CLAIM_POS");
    }

    public static ArrayList<String> getChallengeQue(String kingdomId) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomId);
        ArrayList<String> challenges = new ArrayList<>();
        for (JsonElement challenge : kingdom.getJson("CHALLENGE_QUE").getAsJsonArray()) {
            challenges.add(challenge.getAsString());
        }
        return challenges;
    }

    public static ArrayList<String> getCompletedChallenges(String kingdomId) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomId);
        ArrayList<String> challenges = new ArrayList<>();
        for (JsonElement challenge : kingdom.getJson("COMPLETED_CHALLENGES").getAsJsonArray()) {
            challenges.add(challenge.getAsString());
        }
        return challenges;
    }

    public static long endTimeOfLastRevolution(String kingdomID) {
        DataContainer kingdom = KingdomProcedures.kingdomData.get(kingdomID);
        return kingdom.getLong("endTimeOfLastRevolution");
    }
}
