package arnaria.kingdoms.systems.procedures;

import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VerificationProcedures {

    private static final HashMap<String, UUID> verificationRequests = new HashMap<>();
    private static final Table linkedAccounts = new Table("LinkedAccounts");

    public static void addVerificationRequest(String token, UUID uuid) {
        for (DataContainer linkedAccount : linkedAccounts.getDataContainers()) {
            if (linkedAccount.getId().equals(uuid.toString())) return;
        }
        verificationRequests.put(token, uuid);
        //Send notification to user in game
        NotificationManager.send(uuid, "A request has been made from our website to link to your minecraft account.", NotificationTypes.WARN);
        NotificationManager.send(uuid, Text.Serializer.fromJson("{\"text\":\"CLICK TO LINK ACCOUNTS\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/verify\"}}"), NotificationTypes.ACHIEVEMENT);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable requestExpireTask = () -> removeVerificationRequest(token);
        scheduler.schedule(requestExpireTask, 15, TimeUnit.MINUTES);
    }

    public static void removeVerificationRequest(String token) {
        verificationRequests.remove(token);
    }

    public static boolean verifyUser(String token, UUID uuid) {
        if (verificationRequests.get(token).equals(uuid)) {
            verificationRequests.remove(token);
            DataContainer linkedAccount = new DataContainer(uuid.toString());
            linkedAccounts.put(linkedAccount);
            linkedAccount.put("TOKEN", token);
            return true;
        }
        return false;
    }

    public static UUID getUuid(String token) {
        for (DataContainer linkedAccount : linkedAccounts.getDataContainers()) {
            if (linkedAccount.getString("TOKEN").equals(token)) return UUID.fromString(linkedAccount.getId());
        }
        return null;
    }
}
