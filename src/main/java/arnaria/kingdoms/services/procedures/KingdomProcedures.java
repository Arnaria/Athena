package arnaria.kingdoms.services.procedures;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.claims.ClaimManager;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.InterfaceTypes;
import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;

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
        kingdom.put("REQUESTS", new JsonArray());
        kingdom.put("BLOCKED", new JsonArray());
        kingdom.put("CLAIM_MARKER_POINTS_TOTAL", 1);
        kingdom.put("CLAIM_MARKER_POINTS_USED", 0);

        addMember(kingdomId, uuid);

        PlayerEntity executor = playerManager.getPlayer(uuid);
        if (executor != null) ((PlayerEntityInf) executor).setKingship(true);

        NotificationManager.send(uuid, "You are now the leader of " + kingdomId, NotificationTypes.ACHIEVEMENT);
    }

    public static void disbandKingdom(Enum<InterfaceTypes> platform, String kingdomId, UUID uuid) {
        if (!kingdomId.isEmpty()) {
            if (KingdomsData.getKing(kingdomId).equals(uuid)) {
                kingdomData.drop(kingdomId);
                ClaimManager.dropClaims(kingdomId);
                List<ServerPlayerEntity> onlinePlayers = playerManager.getPlayerList();
                for (ServerPlayerEntity player : onlinePlayers) {
                    if (((PlayerEntityInf) player).getKingdomId().equals(kingdomId)) {
                        ((PlayerEntityInf) player).setKingdomId("");
                        ((PlayerEntityInf) player).setKingship(false);
                    }
                }
                if (!platform.equals(InterfaceTypes.API)) NotificationManager.send(uuid, kingdomId + " has been disbanded", NotificationTypes.WARN);
                else {
                    // API STUFF LOL!!
                }
            } else {
                if (!platform.equals(InterfaceTypes.API)) NotificationManager.send(uuid, "Only the leader can run this command", NotificationTypes.WARN);
                else {
                    // API STUFF LOL!!
                }
            }
        } else {
            if (!platform.equals(InterfaceTypes.API)) NotificationManager.send(uuid, "You are not in a kingdom", NotificationTypes.WARN);
            else {
                // API STUFF LOL!!
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
                NotificationManager.send(uuid, "You are now the leader of " + kingdomId + "!", NotificationTypes.ACHIEVEMENT);
            }
        }
    }

    public static void setColor(Enum<InterfaceTypes> platform, String kingdomId, Formatting color, UUID uuid) {
        if (!kingdomId.isEmpty()) {
            if (KingdomsData.getKing(kingdomId).equals(uuid)) {
                DataContainer kingdom = kingdomData.get(kingdomId);
                kingdom.put("COLOR", color.getName());
                ClaimManager.updateClaimTagColor(kingdomId, color.getName());
                if (!platform.equals(InterfaceTypes.API))
                    NotificationManager.send(uuid, kingdomId + "'s colour is now " + color, NotificationTypes.ACHIEVEMENT);
                else {
                    // API STUFF LOL!!
                }

            } else {
                if (!platform.equals(InterfaceTypes.API))
                    NotificationManager.send(uuid, "Only the leader can run this command", NotificationTypes.WARN);
                else {
                    // API STUFF LOL!!
                }
            }
        } else {
            if (!platform.equals(InterfaceTypes.API))
                NotificationManager.send(uuid, "You are not in a kingdom", NotificationTypes.WARN);
            else {
                // API STUFF LOL!!
            }
        }
    }

    public static void addJoinRequest(Enum<InterfaceTypes> platform, String kingdomID, UUID uuid) {
        DataContainer kingdom = kingdomData.get(kingdomID);
        JsonArray requests = kingdom.getJson("REQUESTS").getAsJsonArray();
        for (JsonElement request : requests) {
            if (request.getAsString().equals(uuid.toString())) return;
        }

        requests.add(uuid.toString());
        kingdom.put("REQUESTS", requests);
    }

    public static void removeJoinRequest(Enum<InterfaceTypes> platform, String kingdomID, UUID executor, UUID player) {
        if (!kingdomID.isEmpty()) {
            if (KingdomsData.getKing(kingdomID).equals(executor)) {
                if (KingdomsData.getJoinRequests(kingdomID).contains(player)) {
                    DataContainer kingdom = kingdomData.get(kingdomID);
                    JsonArray requests = kingdom.getJson("REQUESTS").getAsJsonArray();

                    int index = 0;
                    for (JsonElement request : requests) {
                        if (request.getAsString().equals(executor.toString())) requests.remove(index);
                        index++;
                    }
                    kingdom.put("REQUESTS", requests);

                    if (!platform.equals(InterfaceTypes.API)) {
                        NotificationManager.send(executor, playerManager.getPlayer(player) + " has been denied from joining " + kingdomID, NotificationTypes.INFO);
                        NotificationManager.send(player, "Your request to join " + kingdomID + " has been denied", NotificationTypes.WARN);
                    }
                    else {
                        // API STUFF LOL!!
                    }

                } else {
                    if (!platform.equals(InterfaceTypes.API)) NotificationManager.send(executor,playerManager.getPlayer(player) + " has not requested to join " + kingdomID, NotificationTypes.WARN);
                    else {
                        //API STUFF LOL!
                    }
                }

            } else {
                if (!platform.equals(InterfaceTypes.API)) {
                    NotificationManager.send(executor, "Only a Leader con run this command", NotificationTypes.WARN);

                } else {
                    // API STUFF LOL!!
                }
            }

        } else {
            if (!platform.equals(InterfaceTypes.API)) {
                NotificationManager.send(executor, "You are not in a kingdom", NotificationTypes.WARN);

            } else {
                // API STUFF LOL!!
            }
        }
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

    public static void blockPlayer(Enum<InterfaceTypes> platform, String kingdomID, UUID executor, UUID player) {
        if (!kingdomID.isEmpty()) {
            if (KingdomsData.getKing(kingdomID).equals(executor)) {
                if (!KingdomsData.getBlockedPlayers(kingdomID).contains(player)) {
                    DataContainer kingdom = kingdomData.get(kingdomID);
                    JsonArray blockedPlayer = kingdom.getJson("BLOCKED").getAsJsonArray();

                    if (KingdomsData.getMembers(kingdomID).contains(player)) KingdomProcedures.removeMember(kingdomID, player);
                    blockedPlayer.add(player.toString());
                    kingdom.put("BLOCKED", blockedPlayer);

                    if (!platform.equals(InterfaceTypes.API)) {
                        NotificationManager.send(executor, playerManager.getPlayer(player) + " has been blocked from joining " + kingdomID, NotificationTypes.EVENT);
                        NotificationManager.send(player, "You have been blocked from joining " + kingdomID, NotificationTypes.WARN);

                    } else {
                        // API STUFF LOL!!
                    }

                } else {
                    if (!platform.equals(InterfaceTypes.API)) {
                        NotificationManager.send(executor, playerManager.getPlayer(player) + " is already blocked from joining " + kingdomID, NotificationTypes.WARN);

                    } else {
                        // API STUFF LOL!!
                    }
                }

            } else {
                if (!platform.equals(InterfaceTypes.API)) {
                    NotificationManager.send(executor, "Only a Leader con run this command", NotificationTypes.WARN);

                } else {
                    // API STUFF LOL!!
                }
            }

        } else {
            if (!platform.equals(InterfaceTypes.API)) {
                NotificationManager.send(executor, "You are not in a kingdom", NotificationTypes.WARN);

            } else {
                // API STUFF LOL!!
            }
        }
    }

    public static void unblockPlayer(Enum<InterfaceTypes> platform, String kingdomID, UUID executor, UUID player) {

    }

    public static void addClaimMarkerPointsTotal(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int originalAmount = kingdom.getInt("CLAIM_MARKER_POINTS_TOTAL");
        kingdom.put("CLAIM_MARKER_POINTS_TOTAL", originalAmount - amount);
    }

    public static void removeClaimMarkerPointsTotal(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int originalAmount = kingdom.getInt("CLAIM_MARKER_POINTS_TOTAL");
        kingdom.put("CLAIM_MARKER_POINTS_TOTAL", originalAmount - amount);
    }

    public static void addClaimMarkerPointsUsed(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int originalAmount = kingdom.getInt("CLAIM_MARKER_POINTS_USED");
        kingdom.put("CLAIM_MARKER_POINTS_USED", originalAmount + amount);
    }

    public static void removeClaimMarkerPointsUsed(String kingdomId, int amount) {
        DataContainer kingdom = kingdomData.get(kingdomId);
        int originalAmount = kingdom.getInt("CLAIM_MARKER_POINTS_USED");
        kingdom.put("CLAIM_MARKER_POINTS_TOTAL_USED", originalAmount - amount);
    }
}