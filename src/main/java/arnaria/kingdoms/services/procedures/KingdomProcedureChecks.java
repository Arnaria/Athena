package arnaria.kingdoms.services.procedures;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.InterfaceTypes;
import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;

import java.util.UUID;

public class KingdomProcedureChecks {

    public static void createKingdom(Enum<InterfaceTypes> platform, String kingdomId, UUID executor) {
        if (!kingdomId.isEmpty()) {
            for (String kingdom : KingdomsData.getKingdomIds()) {
                if (KingdomsData.getMembers(kingdom).contains(executor)) {
                    if (platform.equals(InterfaceTypes.COMMAND)) {
                        NotificationManager.send(executor, "You are already in a kingdom", NotificationTypes.ERROR);
                    } else {
                        // API STUFF LOL!!!
                    }
                    return;
                }

                if (kingdom.equalsIgnoreCase(kingdomId)) {
                    if (platform.equals(InterfaceTypes.COMMAND)) {
                        NotificationManager.send(executor, kingdomId + "already exists", NotificationTypes.ERROR);
                    } else {
                        // API STUFF LOL!!!
                    }
                    return;
                }
            }
            KingdomProcedures.createKingdom(kingdomId, executor);
        }
    }

    public static void disbandKingdom(Enum<InterfaceTypes> platform, String kingdomId, UUID executor) {
        if (!kingdomId.isEmpty()) {
            if (KingdomsData.getKing(kingdomId).equals(executor)) {
                KingdomProcedures.removeKingdom(kingdomId);
                if (!platform.equals(InterfaceTypes.API)) NotificationManager.send(executor, kingdomId + " has been disbanded", NotificationTypes.WARN);
                else {
                    // API STUFF LOL!!
                }
            } else {

                if (!platform.equals(InterfaceTypes.API)) NotificationManager.send(executor, "Only the leader can run this command", NotificationTypes.WARN);
                else {
                    // API STUFF LOL!!
                }
            }
        } else {

        }
    }

    public static void transferKingShip(Enum<InterfaceTypes> platform, String kingdomID, UUID executor, UUID player) {
        if (!kingdomID.isEmpty()) {
            if (executor.equals(KingdomsData.getKing(kingdomID))) {
                if (!KingdomsData.getMembers(kingdomID).contains(player))
                    for (String kingdom : KingdomsData.getKingdomIds()) {
                        if (KingdomsData.getMembers(kingdom).contains(player)) {
                            if (KingdomsData.getKing(kingdom).equals(player)) KingdomProcedures.combineKingdoms(kingdomID, kingdom);
                                if (platform.equals(InterfaceTypes.COMMAND)) {
                                    NotificationManager.send(executor, "You are already in a kingdom", NotificationTypes.ERROR);
                                } else {
                                    // API STUFF LOL!!!
                                }
                            return;
                        }
                    } else {
                        KingdomProcedures.updateKing(kingdomID, player);
                }
            } else {

            }

        }
    }

}
