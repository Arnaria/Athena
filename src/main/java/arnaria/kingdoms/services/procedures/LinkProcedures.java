package arnaria.kingdoms.services.procedures;

import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkProcedures {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final HashMap<String, String> linkRequests = new HashMap<>();
    private static final Table linkedAccounts = new Table("LinkedAccounts");

    public static String getValidLinkToken() {
        String linkToken = RandomStringUtils.randomAlphanumeric(10);
        while (linkRequests.containsKey(linkToken)) linkToken = RandomStringUtils.randomAlphanumeric(10);
        return linkToken;
    }

    public static void createLinkRequest(String userToken, String linkToken, UUID uuid) {
        if (linkedAccounts.get(userToken) == null) {
            linkRequests.put(linkToken, userToken);
            NotificationManager.send(uuid, "A link request was made to your account! Run /link <linkToken> to link accounts!", NotificationTypes.INFO);

            Runnable removeLinkRequest = () -> linkRequests.remove(linkToken);
            ScheduledFuture<?> cancelTask = scheduler.schedule(removeLinkRequest, 15, TimeUnit.MINUTES);
            cancelTask.cancel(false);
        }
        // we need to tell api that the uuid is already linked to an account here
    }

    public static void linkAccounts(String linkToken, UUID uuid) {
        if (linkRequests.containsKey(linkToken)) {
            DataContainer accounts = new DataContainer(linkRequests.remove(linkToken));
            linkedAccounts.put(accounts);

            accounts.put("UUID", uuid);

            NotificationManager.send(uuid, "Successfully linked accounts!", NotificationTypes.INFO);
        } else NotificationManager.send(uuid, "Invalid link token. Go to the website and try again", NotificationTypes.ERROR);
    }

    public static UUID getAccount(String userToken) {
        DataContainer accounts = linkedAccounts.get(userToken);
        return accounts.getUuid("UUID");
    }
}
