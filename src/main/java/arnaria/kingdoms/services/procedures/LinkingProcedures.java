package arnaria.kingdoms.services.procedures;

import arnaria.kingdoms.util.BetterPlayerManager;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.text.Text;
import org.apache.commons.text.RandomStringGenerator;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static arnaria.kingdoms.Kingdoms.database;

public class LinkingProcedures {

    private static final Table linkedAccounts = database.createTable("LinkedAccounts");
    private static final HashMap<String, AccountLink> linkRequests = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();

    public static boolean tokenInService(String token) {
        return linkRequests.containsKey(token);
    }

    public static boolean accountLinked(UUID uuid) {
        for (DataContainer accounts : linkedAccounts.getDataContainers()) {
            if (accounts.getUuid("UUID").equals(uuid)) return true;
        }
        return false;
    }

    public static String getValidLinkToken() {
        String linkToken = generator.generate(10);
        while (tokenInService(linkToken)) linkToken = generator.generate(10);
        return linkToken;
    }

    public static void createLinkRequest(String accessToken, String email, String username) {
        UUID uuid = BetterPlayerManager.getUuid(username);

        if (!linkedAccounts.contains(accessToken) && uuid != null) {
            String linkToken = getValidLinkToken();

            for (String token : linkRequests.keySet()) {
                if (linkRequests.get(token).accessToken().equals(accessToken)) {
                    linkRequests.remove(linkToken);
                    break;
                }
            }

            linkRequests.put(linkToken, new AccountLink(accessToken, email, username));
            scheduler.schedule(() -> linkRequests.remove(linkToken), 15, TimeUnit.MINUTES);

            NotificationManager.send(uuid, Text.Serializer.fromJson("{\"text\":\"The account under " + email + " wants to link to your minecraft account. Click this message to accept\",\"color\":\"#00ff00\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/link " + linkToken + "\"}}"), NotificationTypes.INFO);
        }
    }

    public static void acceptLinkRequest(String linkToken, UUID uuid) {
        AccountLink accountLink = linkRequests.get(linkToken);
        if (accountLink != null && BetterPlayerManager.getUuid(accountLink.username()).equals(uuid)) {
            DataContainer accounts = linkedAccounts.createDataContainer(accountLink.accessToken());
            accounts.put("UUID", uuid);
            accounts.put("EMAIL", accountLink.email());
            linkRequests.remove(linkToken);
            NotificationManager.send(uuid, "Successfully linked your accounts!", NotificationTypes.ACHIEVEMENT);
        }
    }

    public static void declineLinkRequest(String linkToken) {
        linkRequests.remove(linkToken);
    }

    public static String getLinkedEmail(UUID uuid) {
        for (DataContainer accounts : linkedAccounts.getDataContainers()) {
            if (accounts.getUuid("UUID").equals(uuid)) {
                return accounts.getString("EMAIL");
            }
        }
        return null;
    }

    public static void unlinkAccounts(String accessToken) {
        DataContainer accounts = linkedAccounts.get(accessToken);

        if (accounts != null) {
            NotificationManager.send(accounts.getUuid("UUID"), "Your account is no longer linked", NotificationTypes.WARN);
            linkedAccounts.drop(accounts);
        }
    }

    public static void unlinkAccounts(UUID uuid) {
        for (DataContainer accounts : linkedAccounts.getDataContainers()) {
            if (accounts.getUuid("UUID").equals(uuid)) {
                NotificationManager.send(accounts.getUuid("UUID"), "Your account is no longer linked", NotificationTypes.WARN);
                linkedAccounts.drop(accounts.getId());
                break;
            }
        }
    }

    public static UUID getUuid(String accessToken) {
        if (linkedAccounts.contains(accessToken)) {
            DataContainer accounts = linkedAccounts.get(accessToken);
            return accounts.getUuid("UUID");
        }
        return null;
    }
}
