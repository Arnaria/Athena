package arnaria.kingdoms.util.procedures;

import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RestProcedures {

    private static final HashMap<String, UUID> verificationRequests = new HashMap<>();
    private static final Table linkedAccounts = new Table("LinkedAccounts");

    public static void addVerificationRequest(String token, UUID uuid) {
        verificationRequests.put(token, uuid);
        //Send notification to user in game

        Runnable requestExpireTask = () -> removeVerificationRequest(token);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
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
}
