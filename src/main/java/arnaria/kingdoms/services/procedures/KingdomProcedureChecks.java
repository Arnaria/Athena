package arnaria.kingdoms.services.procedures;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.InterfaceTypes;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import net.minecraft.util.Formatting;

import static arnaria.kingdoms.Kingdoms.playerManager;
import static arnaria.kingdoms.Kingdoms.userCache;

import java.util.Objects;
import java.util.UUID;

public class KingdomProcedureChecks {

    public static void sendNotification(Enum<InterfaceTypes> platform, UUID receiver, String message, String notificationType) {
        if (platform.equals(InterfaceTypes.COMMAND)) {
            NotificationManager.send(receiver, message, notificationType);
        } else {
            // API STUFF LOL!!!
        }
    }


    public static void createKingdom(Enum<InterfaceTypes> platform, String kingdomID, UUID executor) {
        if (!kingdomID.isEmpty()) {
            for (String kingdom : KingdomsData.getKingdomIds()) {
                if (KingdomsData.getMembers(kingdom).contains(executor)) {
                    sendNotification(platform, executor, "You are already in a kingdom", NotificationTypes.ERROR);
                    return;
                }

                if (kingdom.equalsIgnoreCase(kingdomID)) {
                    sendNotification(platform, executor, kingdomID + " already exists", NotificationTypes.ERROR);
                    return;
                }
            }
            KingdomProcedures.createKingdom(kingdomID, executor);
        }
    }

    public static void disbandKingdom(Enum<InterfaceTypes> platform, String kingdomId, UUID executor) {
        if (!kingdomId.isEmpty()) {
            if (KingdomsData.getKing(kingdomId).equals(executor)) {
                for (UUID player : KingdomsData.getMembers(kingdomId))
                    sendNotification(platform, player, kingdomId + " has been disbanded", NotificationTypes.WARN);
                KingdomProcedures.removeKingdom(kingdomId);
                sendNotification(platform, executor, kingdomId + " has been disbanded", NotificationTypes.WARN);
            } else {
                sendNotification(platform, executor, "Only the leader can run this command", NotificationTypes.WARN);
            }
        } else {
            sendNotification(platform, executor, "Only the leader can run this command", NotificationTypes.WARN);
        }
    }

    public static void transferKingShip(Enum<InterfaceTypes> platform, String kingdomID, UUID executor, UUID player) {
        if (!kingdomID.isEmpty()) {
            if (executor.equals(KingdomsData.getKing(kingdomID))) {
                if (!KingdomsData.getMembers(kingdomID).contains(player))
                    for (String kingdom : KingdomsData.getKingdomIds()) {
                        if (KingdomsData.getMembers(kingdom).contains(player)) {
                            if (KingdomsData.getKing(kingdom).equals(player)) {
                                KingdomProcedures.combineKingdoms(kingdomID, kingdom);
                                userCache.getByUuid(player).ifPresent(gameProfile -> sendNotification(platform, executor, "Leadership of " + kingdomID + " has been transferred to " + gameProfile.getName(), NotificationTypes.EVENT));
                                sendNotification(platform, player, "You have been made the leader of " + kingdomID, NotificationTypes.EVENT);
                                return;
                            }
                            if (platform.equals(InterfaceTypes.COMMAND)) {
                                NotificationManager.send(executor, "You can only transfor your kingdom to a player not alligned with a kingdom, in your kingdom, or the king of another kingdom", NotificationTypes.ERROR);
                            } else {
                                // API STUFF LOL!!!
                            }
                            return;
                        }
                    } else {
                        KingdomProcedures.updateKing(kingdomID, player);
                    if (platform.equals(InterfaceTypes.COMMAND)) {
                        NotificationManager.send(executor, "Leadership of " + kingdomID + " has been given to " + player, NotificationTypes.ERROR);
                    } else {
                        // API STUFF LOL!!!
                    }
                }
            } else {
                if (platform.equals(InterfaceTypes.COMMAND)) {
                    NotificationManager.send(executor, "You are not a leader of a kingdom", NotificationTypes.ERROR);
                } else {
                    // API STUFF LOL!!!
                }
            }
        }
    }

    public static void setColour(Enum<InterfaceTypes> platform, String kingdomID, Formatting color, UUID executor) {
        if(!kingdomID.isEmpty()) {
            if (KingdomsData.getKing(kingdomID).equals(executor)) {
                KingdomProcedures.setColor(kingdomID, color);
                if (!platform.equals(InterfaceTypes.API))
                    NotificationManager.send(executor, kingdomID + "'s colour is now " + color.name(), NotificationTypes.EVENT);
                else {
                    // API STUFF LOL!!
                }

            } else {
                if (!platform.equals(InterfaceTypes.API))
                    NotificationManager.send(executor, "Only the leader can run this command", NotificationTypes.WARN);
                else {
                    // API STUFF LOL!!
                }
            }
        } else {
            if (!platform.equals(InterfaceTypes.API))
                NotificationManager.send(executor, "You are not in a kingdom", NotificationTypes.WARN);
            else {
                // API STUFF LOL!!
            }
        }
    }

    public static void addJoinRequest(Enum<InterfaceTypes> platform, String kingdomID, UUID executor) {
        if (kingdomID.isEmpty()) {
            if (!KingdomsData.getMembers(kingdomID).contains(executor)) {
                if (!KingdomsData.getBlockedPlayers(kingdomID).contains(executor)) {
                    KingdomProcedures.addJoinRequest(kingdomID, executor);

                } else {
                    if (!platform.equals(InterfaceTypes.API))
                        NotificationManager.send(executor, "You are not in a kingdom", NotificationTypes.WARN);
                    else {
                        // API STUFF LOL!!
                    }
                }
            } else {
                if (!platform.equals(InterfaceTypes.API))
                    NotificationManager.send(executor, "You are already in " + kingdomID, NotificationTypes.WARN);
                else {
                    // API STUFF LOL!!
                }
            }
        } else {
            if (!platform.equals(InterfaceTypes.API))
                NotificationManager.send(executor, "Please select a Kingdom", NotificationTypes.WARN);
            else {
                // API STUFF LOL!!
            }
        }
    }

    public static void acceptJoinRequest(Enum<InterfaceTypes> platform, String kingdomID, UUID executor, UUID player) {
        if (!kingdomID.isEmpty()) {
            if (KingdomsData.getKing(kingdomID).equals(executor)){
                if (!KingdomsData.getJoinRequests(kingdomID).contains(player)) {
                    KingdomProcedures.addMember(kingdomID, player);
                    KingdomProcedures.removeJoinRequest(kingdomID, player);
                    if (!platform.equals(InterfaceTypes.API)) {
                        userCache.getByUuid(player).ifPresent(gameProfile -> sendNotification(platform, executor, gameProfile.getName() + " has joined your kingdom", NotificationTypes.EVENT));
                        sendNotification(platform, player, "You have been accepted into " + kingdomID, NotificationTypes.EVENT);
                    }
                    else {
                        // API STUFF LOL!!
                    }
                }
            }
        }
    }

    public static void declineJoinRequest(Enum<InterfaceTypes> platform, String kingdomID, UUID executor, UUID player) {
        if (!kingdomID.isEmpty()) {
            if (KingdomsData.getKing(kingdomID).equals(executor)) {
                if (KingdomsData.getJoinRequests(kingdomID).contains(player)) {
                    KingdomProcedures.removeJoinRequest(kingdomID, player);
                    if (!platform.equals(InterfaceTypes.API))
                        NotificationManager.send(executor, playerManager.getPlayer(player).toString() + " request has been declined", NotificationTypes.WARN);
                    else {
                        // API STUFF LOL!!
                    }
                } else {
                    if (!platform.equals(InterfaceTypes.API))
                        NotificationManager.send(executor, playerManager.getPlayer(player).toString() + " hasnt requested to join your kingdom", NotificationTypes.WARN);
                    else {
                        // API STUFF LOL!!
                    }
                }
            }
        }
    }
}
