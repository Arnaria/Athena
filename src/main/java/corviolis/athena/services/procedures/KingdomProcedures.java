package corviolis.athena.services.procedures;

import corviolis.athena.Athena;
import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.interfaces.ScoreboardInf;
import corviolis.athena.services.claims.ClaimManager;
import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.services.events.Challenge;
import corviolis.athena.services.events.ChallengeManager;
import corviolis.athena.services.events.EventManager;
import corviolis.athena.util.BetterPlayerManager;
import corviolis.athena.services.api.BlueMapAPI;
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
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;

import java.util.*;

import static corviolis.athena.Athena.scoreboard;
import static corviolis.athena.Athena.settings;

public class KingdomProcedures {

    public static final Table kingdomData = Athena.database.createTable("KingdomData");

    public static void init() {
        ((ScoreboardInf) scoreboard).clearTeams();

        for (DataContainer kingdom : kingdomData.getDataContainers()) {
            Team kingdomTeam = scoreboard.addTeam(kingdom.getId());
            kingdomTeam.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS);
            kingdomTeam.setFriendlyFireAllowed(false);
            Formatting color = Formatting.byName(KingdomsData.getColor(kingdom.getId()));
            if (color != null) kingdomTeam.setColor(color);
        }

        if (!kingdomData.contains("ADMIN")) {
            createKingdom("ADMIN", Util.NIL_UUID);
            setColor("ADMIN", Formatting.WHITE);
        }
        for (String admin : settings.ADMINS) addMember("ADMIN", UUID.fromString(admin));
    }

    public static void setupPlayer(PlayerEntity player) {
        for (DataContainer kingdom : kingdomData.getDataContainers()) {
            String kingdomId = kingdom.getId();
            if (KingdomsData.getMembers(kingdomId).contains(player.getUuid())) {
                if (!kingdomId.equals("ADMIN")) {
                    if (kingdom.getUuid("KING").equals(player.getUuid())) ((PlayerEntityInf) player).setKingship(true);
                    ((PlayerEntityInf) player).setKingdomId(kingdomId);
                } else {
                    ((PlayerEntityInf) player).allowToEditIn(kingdomId);
                }
            }
        }
    }

    public static void createKingdom(String kingdomId, UUID uuid) {
        Team kingdomTeam = scoreboard.addTeam(kingdomId);
        kingdomTeam.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.HIDE_FOR_OTHER_TEAMS);
        kingdomTeam.setFriendlyFireAllowed(false);
        Formatting color = Formatting.byColorIndex((int) (Math.random() * 15));
        kingdomTeam.setColor(color);
        Date time = new Date();

        DataContainer kingdom = kingdomData.createDataContainer(kingdomId);
        if (color != null) kingdom.put("COLOR", color.getName());
        else kingdom.put("COLOR", "white");
        kingdom.put("BANNER_COUNT", 0);
        kingdom.put("XP", 0);
        kingdom.put("MEMBERS", new JsonArray());
        kingdom.put("REQUESTS", new JsonArray());
        kingdom.put("BLOCKED", new JsonArray());
        kingdom.put("ADVISERS", new JsonArray());
        kingdom.put("COMPLETED_CHALLENGES", new JsonArray());
        kingdom.put("CHALLENGE_QUE", new JsonArray());
        kingdom.put("endTimeOfLastRevolution", time.getTime());

        if (!uuid.equals(Util.NIL_UUID)) {
            kingdom.put("KING", uuid);
            addMember(kingdomId, uuid);
            PlayerEntity executor = BetterPlayerManager.getPlayer(uuid);
            if (executor != null) ((PlayerEntityInf) executor).setKingship(true);
        }
    }

    public static void removeKingdom(String kingdomId) {
        ClaimManager.dropKingdom(kingdomId);
        scoreboard.removeTeam(scoreboard.getTeam(kingdomId));
        BlueMapAPI.getMarkerApi().removeMarkerSet(kingdomId);
        BlueMapAPI.saveMarkers();

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

    public static void combineKingdoms(String kingdomId, String newKingdomId) {
        ClaimManager.transferClaims(kingdomId, newKingdomId);
        for (UUID player : KingdomsData.getMembers(kingdomId)) addMember(newKingdomId, player);
    }

    public static void setColor(String kingdomId, Formatting color) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        Team kingdomTeam = scoreboard.getTeam(kingdomId);
        kingdom.put("COLOR", color.getName());
        if (kingdomTeam != null) kingdomTeam.setColor(color);
        ClaimManager.updateColor(kingdomId, color.getName());
    }

    //This may or may not work
    public static void renameKingdom(String kingdomId, String newKingdomId, UUID executor) {
        createKingdom(newKingdomId, executor);
        combineKingdoms(kingdomId, newKingdomId);
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

        if (KingdomsData.getAdvisers(kingdomID).contains(uuid)) {
            removeAdviser(kingdomID, uuid);
        }

        PlayerEntity player = BetterPlayerManager.getPlayer(uuid);
        if (player != null) ((PlayerEntityInf) player).removeAllowedToEditIn(kingdomID);
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

    public static void addXp(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int newAmount = kingdom.getInt("XP") + amount;
        kingdom.put("XP", newAmount);
    }

    public static void setStartingClaimPos(String kingdomId, BlockPos pos) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        kingdom.put("STARTING_CLAIM_POS", pos);
    }

    public static void addChallengeToQue(String kingdomId, String challengeId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        JsonArray challengeQue = kingdom.getJson("CHALLENGE_QUE").getAsJsonArray();
        challengeQue.add(challengeId);
        kingdom.put("CHALLENGE_QUE", challengeQue);
    }

    public static void removeChallengeFromQue(String kingdomId, String challengeId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        JsonArray challengeQue = kingdom.getJson("CHALLENGE_QUE").getAsJsonArray();

        int count = 0;
        for (JsonElement challenge : challengeQue) {
            if (challenge.getAsString().equals(challengeId)) break;
            count++;
        }

        challengeQue.remove(count);
        kingdom.put("CHALLENGE_QUE", challengeQue);
    }

    public static void completeChallenge(String kingdomId, String challengeId) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        JsonArray completedChallenges = kingdom.getJson("COMPLETED_CHALLENGES").getAsJsonArray();
        removeChallengeFromQue(kingdomId, challengeId);
        completedChallenges.add(challengeId);
        Challenge challenge = ChallengeManager.getChallenge(challengeId);
        if (challenge != null) addXp(kingdomId, challenge.xp());
    }

    public static String getKingdomId(UUID uuid) {
        for (String kingdomId : kingdomData.getIds()) {
            if (!kingdomId.equals("ADMIN") && KingdomsData.getMembers(kingdomId).contains(uuid)) return kingdomId;
        }
        return null;
    }

    public static void startRevolution(String kingdomID) {
        EventManager.startRevolution(kingdomID);
    }

    public static void endRevolution(String kingdomID) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        Date time = new Date();
        kingdom.put("endTimeOfLastRevolution", time.getTime());
    }
}