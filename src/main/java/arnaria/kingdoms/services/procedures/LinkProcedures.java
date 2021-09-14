package arnaria.kingdoms.services.procedures;

import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
import io.leangen.geantyref.CaptureType;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.text.Text;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LinkProcedures {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final HashMap<String, String> linkRequests = new HashMap<>();
    private static final Table linkedAccounts = new Table("LinkedAccounts");

    private static String getLinkToken() {
        byte[] array = new byte[12];
        new Random().nextBytes(array);
        return new String(array, StandardCharsets.ISO_8859_1);
    }

    public static String getValidLinkToken() {
        String linkToken = getLinkToken();
        while (linkRequests.containsKey(linkToken)) linkToken = getLinkToken();
        return linkToken;
    }

    public static void createLinkRequest(String userToken, String linkToken, UUID uuid) {
        if (linkedAccounts.get(uuid.toString()) == null) {
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
            DataContainer accounts = new DataContainer(uuid.toString());
            linkedAccounts.put(accounts);

            accounts.put("USER_TOKEN", linkRequests.get(linkToken));
            linkRequests.remove(linkToken);

            NotificationManager.send(uuid, "Successfully linked acounts!", NotificationTypes.INFO);
        } else NotificationManager.send(uuid, "Invalid link token. Go to the website and try again", NotificationTypes.ERROR);
    }
}
