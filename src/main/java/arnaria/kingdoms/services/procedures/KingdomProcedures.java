package arnaria.kingdoms.services.procedures;

import arnaria.kingdoms.Kingdoms;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.claims.Claim;
import arnaria.kingdoms.services.claims.ClaimManager;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.events.Challenge;
import arnaria.kingdoms.services.events.ChallengeManager;
import arnaria.kingdoms.util.BetterPlayerManager;
import arnaria.kingdoms.util.BlueMapAPI;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static arnaria.kingdoms.Kingdoms.scoreboard;

public class KingdomProcedures {

    public static final Table kingdomData = Kingdoms.database.createTable("KingdomData");

    public static void setupPlayer(PlayerEntity player) {
        kingdomData.beginTransaction();
        for (DataContainer kingdom : kingdomData.getDataContainers()) {
            if (KingdomsData.getMembers(kingdom.getId()).contains(player.getUuid())) {
                if (kingdom.getUuid("KING").equals(player.getUuid())) ((PlayerEntityInf) player).setKingship(true);
                ((PlayerEntityInf) player).setKingdomId(kingdom.getId());
            }
        }
        kingdomData.endTransaction();
    }

    public static void createKingdom(String kingdomId, UUID uuid) {
        kingdomData.beginTransaction();
        DataContainer kingdom = kingdomData.createDataContainer(kingdomId);
        Team kingdomTeam = scoreboard.addTeam(kingdomId);
        BlueMapAPI.getMarkerApi().createMarkerSet(kingdomId);
        BlueMapAPI.saveMarkers();

        kingdomTeam.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS);
        kingdomTeam.setFriendlyFireAllowed(false);
        kingdom.put("KING", uuid);
        kingdom.put("COLOR", "white");
        kingdom.put("BANNER_COUNT", 0);
        kingdom.put("XP", 0);
        kingdom.put("MEMBERS", new JsonArray());
        kingdom.put("REQUESTS", new JsonArray());
        kingdom.put("BLOCKED", new JsonArray());
        kingdom.put("ADVISERS", new JsonArray());

        addMember(kingdomId, uuid);

        PlayerEntity executor = BetterPlayerManager.getPlayer(uuid);
        if (executor != null) ((PlayerEntityInf) executor).setKingship(true);
        kingdomData.endTransaction();
    }

    public static void removeKingdom(String kingdomId) {
        ClaimManager.dropClaims(kingdomId);
        BlueMapAPI.getMarkerApi().removeMarkerSet(kingdomId);
        BlueMapAPI.saveMarkers();

        /*Team kingdomTeam = scoreboard.getTeam(kingdomId);
        if (kingdomTeam != null) {
            for (String player : kingdomTeam.getPlayerList()) {
                scoreboard.removePlayerFromTeam(player, kingdomTeam);
            }
            scoreboard.removeTeam(kingdomTeam);
        }*/

        scoreboard.removeTeam(scoreboard.getTeam(kingdomId));

        for (ServerPlayerEntity player : BetterPlayerManager.getOnlinePlayers()) {
            if (((PlayerEntityInf) player).getKingdomId().equals(kingdomId)) {
                ((PlayerEntityInf) player).setKingdomId("");
                ((PlayerEntityInf) player).setKingship(false);
            }
        }
        kingdomData.drop(kingdomId);
    }

    public static void updateKing(String kingdomID, UUID king) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        kingdom.put("KING", king);

        for (ServerPlayerEntity player : BetterPlayerManager.getOnlinePlayers()) {
            if (((PlayerEntityInf) player).getKingdomId().equals(kingdomID) && ((PlayerEntityInf) player).isKing()) {
                ((PlayerEntityInf) player).setKingship(false);
            }

            if (player.getUuid().equals(king)) {
                addMember(kingdomID, king);
                ((PlayerEntityInf) player).setKingship(true);
                NotificationManager.send(king, "You are now the leader of " + kingdomID + "!", NotificationTypes.ACHIEVEMENT);
            }
        }
    }

    public static void addAdviser(String kingdomID, UUID player) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        JsonArray advisers = kingdom.getJson("ADVISERS").getAsJsonArray();
        for (JsonElement adviser : advisers) {
            if (adviser.getAsString().equals(player.toString())) return;
        }

        advisers.add(player.toString());
        kingdom.put("ADVISERS", advisers);
    }

    public static void removeAdviser(String kingdomID, UUID player) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        JsonArray advisers = kingdom.getJson("ADVISERS").getAsJsonArray();

        int count = 0;
        for (JsonElement adviser : advisers) {
            if (adviser.getAsString().equals(player.toString())) break;
            count++;
        }

        advisers.remove(count);
        kingdom.put("ADVISERS", advisers);
    }

    public static void combineKingdoms(String deletingKingdom, String keepingKingdom) {
        for (Claim claim : ClaimManager.getClaims(deletingKingdom)) {
            claim.rebrand(keepingKingdom, KingdomsData.getColor(keepingKingdom));
        }
        for (UUID player : KingdomsData.getMembers(deletingKingdom)) {
            addMember(keepingKingdom, player);
        }
    }

    public static void setColor(String kingdomId, Formatting color) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        Team kingdomTeam = scoreboard.getTeam(kingdomId);
        kingdom.put("COLOR", color.getName());
        if (kingdomTeam != null) kingdomTeam.setColor(color);
        ClaimManager.updateClaimColor(kingdomId, color.getName());
    }

    public static void addJoinRequest(String kingdomID, UUID executor) {
            DataContainer kingdom = kingdomData.get(kingdomID);
            JsonArray requests = kingdom.getJson("REQUESTS").getAsJsonArray();
            for (JsonElement request : requests) {
                if (request.getAsString().equals(executor.toString())) return;
            }

            requests.add(executor.toString());
            kingdom.put("REQUESTS", requests);
    }

    public static void removeJoinRequest(String kingdomID, UUID player) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        JsonArray requests = kingdom.getJson("REQUESTS").getAsJsonArray();

        int count = 0;
        for (JsonElement request : requests) {
            if (request.getAsString().equals(player.toString())) break;
            count++;
        }

        requests.remove(count);
        kingdom.put("REQUESTS", requests);
    }

    public static void addMember(String kingdomId, UUID uuid) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        JsonArray members = kingdom.getJson("MEMBERS").getAsJsonArray();

        for (JsonElement member : members) {
            if (member.getAsString().equals(uuid.toString())) return;
        }

        members.add(uuid.toString());
        kingdom.put("MEMBERS", members);
        scoreboard.addPlayerToTeam(BetterPlayerManager.getName(uuid), scoreboard.getTeam(kingdomId));

        for (ServerPlayerEntity player : BetterPlayerManager.getOnlinePlayers()) {
            if (player.getUuid().equals(uuid)) {
                ((PlayerEntityInf) player).setKingdomId(kingdomId);
                break;
            }
        }
    }

    public static void removeMember(String kingdomID, UUID uuid) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        JsonArray members = kingdom.getJson("MEMBERS").getAsJsonArray();

        int count = 0;
        for (JsonElement member : members) {
            if (member.getAsString().equals(uuid.toString())) break;
            count++;
        }

        members.remove(count);
        kingdom.put("MEMBERS", members);
        scoreboard.removePlayerFromTeam(BetterPlayerManager.getName(uuid), scoreboard.getTeam(kingdomID));
    }

    public static void blockPlayer(String kingdomID, UUID player) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        JsonArray blockedPlayer = kingdom.getJson("BLOCKED").getAsJsonArray();

        blockedPlayer.add(player.toString());
        kingdom.put("BLOCKED", blockedPlayer);
    }

    public static void unblockPlayer(String kingdomID, UUID player) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        JsonArray blockedPlayers = kingdom.getJson("BLOCKED").getAsJsonArray();

        int count = 0;
        for (JsonElement blocked : blockedPlayers) {
            if (blocked.getAsString().equals(player.toString())) break;
            count++;
        }

        blockedPlayers.remove(count);
        kingdom.put("BLOCKED", blockedPlayers);
    }

    public static void addToBannerCount(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int originalAmount = kingdom.getInt("BANNER_COUNT");
        kingdom.put("BANNER_COUNT", originalAmount + amount);
    }

    public static void removeFromBannerCount(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int originalAmount = kingdom.getInt("BANNER_COUNT");
        kingdom.put("BANNER_COUNT", originalAmount - amount);
    }

    public static void setStartingBannerPos(String kingdomId, BlockPos pos) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        kingdom.put("STARTING_BANNER_POS", pos);
    }

    public static void addXp(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int newAmount = kingdom.getInt("XP") + amount;
        kingdom.put("XP", newAmount);
    }

    public static void addChallengeToQue(String kingdomId, String challengeId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        ArrayList<String> challenges = (ArrayList<String>) Arrays.asList(kingdom.getStringArray("CHALLENGE_QUE"));
        challenges.add(challengeId);
        kingdom.put("CHALLENGE_QUE", challenges.toArray(new String[]{}));
    }

    public static void completeChallenge(String kingdomId, String challengeId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        ArrayList<String> challenges = (ArrayList<String>) Arrays.asList(kingdom.getStringArray("CHALLENGE_QUE"));
        challenges.remove(challengeId);

        Challenge challenge = ChallengeManager.getChallenge(challengeId);
        if (challenge != null) addXp(kingdomId, challenge.xp());
    }

    public static String getKingdomId(UUID king) {
        for (String kingdomId : kingdomData.getIds()) {
            if (KingdomsData.getKing(kingdomId).equals(king)) return kingdomId;
        }
        return null;
    }
}