package arnaria.kingdoms.services.procedures;

import arnaria.kingdoms.Kingdoms;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.BetterPlayerManager;
import arnaria.kingdoms.util.InterfaceTypes;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;

import java.util.UUID;

public class KingdomProcedureChecks {

    public static void sendNotification(Enum<InterfaceTypes> platform, UUID receiver, String message, String notificationType) {
        if (platform.equals(InterfaceTypes.API)) {
            Kingdoms.log(Level.INFO, "API go brrrrrrr");
        } else {
            NotificationManager.send(receiver, message, notificationType);
        }
    }


    public static void createKingdom(Enum<InterfaceTypes> platform, String kingdomID, UUID executor) {
        if (!kingdomID.isEmpty()) {
            for (String kingdom : KingdomsData.getKingdomIds()) {
                if (KingdomsData.getMembers(kingdom).contains(executor) && !kingdom.equals("ADMIN")) {
                    sendNotification(platform, executor, "You are already in a kingdom", NotificationTypes.ERROR);
                    return;
                }

                if (kingdom.equalsIgnoreCase(kingdomID)) {
                    sendNotification(platform, executor, kingdomID + " already exists", NotificationTypes.ERROR);
                    return;
                }
            }

            KingdomProcedures.createKingdom(kingdomID, executor);
            KingdomProcedures.addMember(kingdomID, executor);
            sendNotification(platform, executor, "You have been crowned king of " + kingdomID, NotificationTypes.ACHIEVEMENT);
        } else sendNotification(platform, executor, "Please provide a kingdom name", NotificationTypes.ERROR);
    }

    public static void disbandKingdom(Enum<InterfaceTypes> platform, String kingdomId, UUID executor) {
        if (!kingdomId.isEmpty()) {
            if (KingdomsData.getKing(kingdomId).equals(executor)) {
                for (UUID player : KingdomsData.getMembers(kingdomId)) {
                    sendNotification(InterfaceTypes.COMMAND, player, kingdomId + " has been disbanded", NotificationTypes.WARN);
                }
                KingdomProcedures.removeKingdom(kingdomId);
            } else sendNotification(platform, executor, "Only the leader can run this command", NotificationTypes.WARN);
        } else sendNotification(platform, executor, "You are not in a kingdom", NotificationTypes.WARN);
    }

    public static void transferKingShip(Enum<InterfaceTypes> platform, String kingdomID, UUID player, UUID executor) {
        if (!kingdomID.isEmpty()) {
            if (executor.equals(KingdomsData.getKing(kingdomID))) {
                if (!KingdomsData.getMembers(kingdomID).contains(player)  && !kingdomID.equals("ADMIN")) {
                    for (String kingdom : KingdomsData.getKingdomIds()) {
                        if (KingdomsData.getMembers(kingdom).contains(player)) {
                            if (KingdomsData.getKing(kingdom).equals(player)) {
                                sendNotification(platform, executor, "Leadership of " + kingdomID + " has been transferred to " + BetterPlayerManager.getName(player), NotificationTypes.EVENT);
                                sendNotification(InterfaceTypes.COMMAND, player, "You have been made the leader of " + kingdomID, NotificationTypes.EVENT);
                                KingdomProcedures.combineKingdoms(kingdomID, kingdom);
                                return;
                            }
                            sendNotification(platform, executor, "You can only transfer your kingdom to a player in your kingdom, not aligned with a kingdom, or the king of another kingdom", NotificationTypes.ERROR);
                            return;
                        }
                    }
                    KingdomProcedures.addMember(kingdomID, player);
                    KingdomProcedures.updateKing(kingdomID, player);
                    sendNotification(platform, executor, "Leadership of " + kingdomID + " has been transferred to " + BetterPlayerManager.getName(player), NotificationTypes.EVENT);
                    sendNotification(InterfaceTypes.COMMAND, player, "You have been made the leader of " + kingdomID, NotificationTypes.EVENT);

                } else {
                    KingdomProcedures.updateKing(kingdomID, player);
                    for (UUID member : KingdomsData.getMembers(kingdomID)) {
                        if (!member.equals(executor) || !member.equals(player)) {
                            sendNotification(InterfaceTypes.COMMAND, member, "Leadership of " + kingdomID + " has been transferred to " + BetterPlayerManager.getName(player), NotificationTypes.EVENT);
                        }
                    }
                    sendNotification(platform, executor, "Leadership of " + kingdomID + " has been transferred to " + BetterPlayerManager.getName(player), NotificationTypes.EVENT);
                    sendNotification(InterfaceTypes.COMMAND, player, "You have been given leadership of " + kingdomID, NotificationTypes.EVENT);
                }
            } else sendNotification(platform, executor, "You are not the leader of " + kingdomID, NotificationTypes.ERROR);

        } else sendNotification(platform, executor, "You are not in a kingdom", NotificationTypes.ERROR);
    }

    public static void addAdviser(Enum<InterfaceTypes> platform, String kingdomID, UUID player, UUID executor) {
        if(!kingdomID.isEmpty()) {
            if (player.equals(executor)) {
                if (KingdomsData.getKing(kingdomID).equals(executor)) {
                    if (KingdomsData.getMembers(kingdomID).contains(player)) {
                        KingdomProcedures.addAdviser(kingdomID, player);
                        sendNotification(platform, executor, BetterPlayerManager.getName(player) + " has been made a adviser of " + kingdomID, NotificationTypes.EVENT);
                        sendNotification(InterfaceTypes.COMMAND, player, "You have been made a Adviser of " + kingdomID, NotificationTypes.EVENT);
                    } else sendNotification(platform, executor, "You can only make a member of your kingdom a adviser", NotificationTypes.WARN);
                } else sendNotification(platform, executor, "Only the leader can run this command", NotificationTypes.WARN);
            } else sendNotification(platform, executor, "You can not make your self an adviser", NotificationTypes.WARN);
        } else sendNotification(platform, executor, "You are not part of a kingdom", NotificationTypes.WARN);
    }

    public static void removeAdviser(Enum<InterfaceTypes> platform, String kingdomID, UUID player, UUID executor) {
        if(!kingdomID.isEmpty()) {
            if (player.equals(executor)) {
                if (KingdomsData.getKing(kingdomID).equals(executor)) {
                    if (KingdomsData.getAdvisers(kingdomID).contains(player)) {
                        KingdomProcedures.removeAdviser(kingdomID, player);
                        sendNotification(platform, executor, BetterPlayerManager.getName(player) + "is no longer a adviser of " + kingdomID, NotificationTypes.WARN);
                        sendNotification(InterfaceTypes.COMMAND, player, "You have been removed as a adviser of " + kingdomID, NotificationTypes.WARN);
                    } else sendNotification(platform, executor, BetterPlayerManager.getName(player) + " is not a adviser for " + kingdomID, NotificationTypes.WARN);
                } else sendNotification(platform, executor, "Only the leader can run this command", NotificationTypes.WARN);
            } else sendNotification(platform, executor, "You can not remove yourself as a adviser", NotificationTypes.WARN);
        } else sendNotification(platform, executor, "You are not part of a kingdom", NotificationTypes.WARN);
    }

    public static void setColour(Enum<InterfaceTypes> platform, String kingdomID, Formatting color, UUID executor) {
        if(!kingdomID.isEmpty()) {
            if (KingdomsData.getKing(kingdomID).equals(executor)) {
                KingdomProcedures.setColor(kingdomID, color);
                sendNotification(platform, executor, kingdomID + "'s colour is now " + color.name(), NotificationTypes.EVENT);

            } else sendNotification(platform, executor, "Only the leader can run this command", NotificationTypes.WARN);
        } else sendNotification(platform, executor, "You are not in a kingdom", NotificationTypes.WARN);
    }

    public static void addJoinRequest(Enum<InterfaceTypes> platform, String kingdomID, UUID executor) {
        if (kingdomID.isEmpty()) {
            if (!KingdomsData.getMembers(kingdomID).contains(executor)) {
                if (!KingdomsData.getBlockedPlayers(kingdomID).contains(executor)) {
                    KingdomProcedures.addJoinRequest(kingdomID, executor);
                    sendNotification(platform, executor, "You have requested to join " + kingdomID, NotificationTypes.INFO);
                    sendNotification(InterfaceTypes.COMMAND, KingdomsData.getKing(kingdomID), BetterPlayerManager.getName(executor) + " has requested to join " + kingdomID, NotificationTypes.EVENT);
                    for (UUID advisers : KingdomsData.getAdvisers(kingdomID)) {
                        sendNotification(InterfaceTypes.COMMAND, advisers, BetterPlayerManager.getName(executor) + " has requested to join " + kingdomID, NotificationTypes.EVENT);
                    }
                } else sendNotification(platform, executor, kingdomID + " has banished you from their kingdom", NotificationTypes.WARN);
            } else sendNotification(platform, executor, "You are already in " + kingdomID, NotificationTypes.WARN);
        } else sendNotification(platform, executor, "Please select a Kingdom", NotificationTypes.WARN);
    }

    public static void acceptJoinRequest(Enum<InterfaceTypes> platform, String kingdomID, UUID player, UUID executor) {
        if (!kingdomID.isEmpty()) {
            if (KingdomsData.getKing(kingdomID).equals(executor) || KingdomsData.getAdvisers(kingdomID).contains(executor)){
                if (!KingdomsData.getJoinRequests(kingdomID).contains(player)) {
                    KingdomProcedures.addMember(kingdomID, player);
                    KingdomProcedures.removeJoinRequest(kingdomID, player);
                    for (String kingdom : KingdomsData.getKingdomIds()) {
                        if (KingdomsData.getJoinRequests(kingdomID).contains(player)) KingdomProcedures.removeJoinRequest(kingdom, player);
                    }
                    sendNotification(platform, executor, BetterPlayerManager.getName(player) + " has joined your kingdom", NotificationTypes.EVENT);
                    sendNotification(platform, player, "You have been accepted into " + kingdomID, NotificationTypes.EVENT);
                }
            } else sendNotification(platform, executor, "Only a leader or adviser can accept new members", NotificationTypes.WARN);
        }
    }

    public static void declineJoinRequest(Enum<InterfaceTypes> platform, String kingdomID, UUID player, UUID executor) {
        if (!kingdomID.isEmpty()) {
            if (KingdomsData.getKing(kingdomID).equals(executor) || KingdomsData.getAdvisers(kingdomID).contains(executor)) {
                if (KingdomsData.getJoinRequests(kingdomID).contains(player)) {
                    KingdomProcedures.removeJoinRequest(kingdomID, player);
                    sendNotification(platform, executor, BetterPlayerManager.getName(player) + " request has been declined", NotificationTypes.WARN);
                    sendNotification(platform, player, "Your request to join " + kingdomID + " has been declined", NotificationTypes.WARN);
                } else sendNotification(platform, executor, BetterPlayerManager.getName(player) + " request has been declined", NotificationTypes.WARN);
            } else sendNotification(platform, executor, "Only a leader or adviser can decline join requests", NotificationTypes.WARN);
        } else sendNotification(platform, executor, "You are not part of a kingdom", NotificationTypes.WARN);
    }

    public static void removePlayer(Enum<InterfaceTypes> platform, String kingdomID, UUID player, UUID executor) {
        if (!kingdomID.isEmpty()){
            if (!executor.equals(player)) {
                if (KingdomsData.getKing(kingdomID).equals(executor)) {
                    KingdomProcedures.removeMember(kingdomID, player);
                    if (KingdomsData.getAdvisers(kingdomID).contains(player)) KingdomProcedures.removeAdviser(kingdomID, player);
                    if (KingdomsData.getJoinRequests(kingdomID).contains(player)) KingdomProcedures.removeJoinRequest(kingdomID, player);
                    sendNotification(platform, executor, BetterPlayerManager.getName(player) + " has been removed from" + kingdomID, NotificationTypes.EVENT);
                    sendNotification(InterfaceTypes.COMMAND, player, "You have been removed from" + kingdomID, NotificationTypes.WARN);
                } else if (KingdomsData.getAdvisers(kingdomID).contains(executor)) {
                    if (KingdomsData.getAdvisers(kingdomID).contains(player)) {
                        KingdomProcedures.removeMember(kingdomID, player);
                        if (KingdomsData.getJoinRequests(kingdomID).contains(player)) KingdomProcedures.removeJoinRequest(kingdomID, player);
                        sendNotification(platform, executor, BetterPlayerManager.getName(player) + " has been removed from" + kingdomID, NotificationTypes.EVENT);
                        sendNotification(InterfaceTypes.COMMAND, player, "You have been removed from" + kingdomID, NotificationTypes.WARN);
                    } else sendNotification(platform, executor, "Only the king can remove advisers", NotificationTypes.WARN);
                } else sendNotification(platform, executor, "Only a king or adviser can rus with command", NotificationTypes.WARN);
            } else sendNotification(platform, executor, "You cant not remove yourself", NotificationTypes.ERROR);
        } else sendNotification(platform, executor, "You are not part of a kingdom", NotificationTypes.ERROR);
    }

    public static void leaveKingdom(Enum<InterfaceTypes> platform, String kingdomID, UUID executor) {
        if (!kingdomID.isEmpty()) {
            if (KingdomsData.getMembers(kingdomID).contains(executor)) {
                if (!KingdomsData.getKing(kingdomID).equals(executor)) {
                    KingdomProcedures.removeMember(kingdomID, executor);
                    sendNotification(platform, executor, "You have left " + kingdomID, NotificationTypes.EVENT);
                } else sendNotification(platform, executor, "A leader can not leave their kingdom, they must either disband their kingdom or transfer their leadership", NotificationTypes.WARN);
            } else sendNotification(platform, executor, "You are not part of this kingdom", NotificationTypes.WARN);
        } else sendNotification(platform, executor, "You are not in a kingdom", NotificationTypes.WARN);
    }

    public static void banishPlayer(Enum<InterfaceTypes> platform, String kingdomID, UUID player, UUID executor) {
        if (!kingdomID.isEmpty()){
            if (KingdomsData.getKing(kingdomID).equals(executor)){
                if (!executor.equals(player)) {
                    KingdomProcedures.blockPlayer(kingdomID, player);
                    if (KingdomsData.getAdvisers(kingdomID).contains(player)) KingdomProcedures.removeAdviser(kingdomID, player);
                    if (KingdomsData.getMembers(kingdomID).contains(player)) KingdomProcedures.removeMember(kingdomID, player);
                    if (KingdomsData.getJoinRequests(kingdomID).contains(player)) KingdomProcedures.removeJoinRequest(kingdomID, player);
                    sendNotification(platform, executor, BetterPlayerManager.getName(player) + " has been banished from " + kingdomID, NotificationTypes.WARN);
                    sendNotification(InterfaceTypes.COMMAND, player, "You have been banished from " + kingdomID, NotificationTypes.WARN);
                } else sendNotification(platform, executor, "You can not banish yourself", NotificationTypes.WARN);
            } else sendNotification(platform, executor, "Only the leader can run this command", NotificationTypes.WARN);
        } else sendNotification(platform, executor, "You are not in a kingdom", NotificationTypes.WARN);
    }

    public static void unBanishPlayer(Enum<InterfaceTypes> platform, String kingdomID, UUID player, UUID executor) {
        if (!kingdomID.isEmpty()){
            if (KingdomsData.getKing(kingdomID).equals(executor)){
                if (KingdomsData.getBlockedPlayers(kingdomID).contains(player)){
                    KingdomProcedures.unblockPlayer(kingdomID, player);
                    sendNotification(platform, executor, BetterPlayerManager.getName(player) + " has not unbanished from " + kingdomID, NotificationTypes.EVENT );
                    sendNotification(InterfaceTypes.COMMAND, player, "You are no longer banished from " + kingdomID, NotificationTypes.EVENT);
                } else sendNotification(platform, executor, BetterPlayerManager.getName(player) + " has not been banished from " + kingdomID, NotificationTypes.WARN );
            } else sendNotification(platform, executor, "Only the leader can run this command", NotificationTypes.WARN);
        } else sendNotification(platform, executor, "You are not in a kingdom", NotificationTypes.WARN);
    }

}
