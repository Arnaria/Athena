package arnaria.kingdoms.services.procedures;

import arnaria.kingdoms.services.api.LinkingAPI;
import arnaria.kingdoms.util.BetterPlayerManager;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import org.apache.commons.text.RandomStringGenerator;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinkingProcedures {

    private static final HashMap<String, LinkRequest> linkRequests = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();

    public static String getValidLinkToken() {
        String linkToken = generator.generate(10);
        while (linkRequests.containsKey(linkToken)) linkToken =generator.generate(10);
        return linkToken;
    }

    public static void createLinkRequest(String accessToken, String email, String username) {
        String linkToken = getValidLinkToken();

        //This may be broken
        for (String token : linkRequests.keySet()) {
            if (linkRequests.get(token).accessToken().equals(accessToken)) {
                linkRequests.remove(linkToken);
                break;
            }
        }

        linkRequests.put(linkToken, new LinkRequest(accessToken, email, username));
        scheduler.schedule(() -> linkRequests.remove(linkToken), 15, TimeUnit.MINUTES);

        NotificationManager.send(BetterPlayerManager.getUuid(username), "The account under " + email + " wants to link to your minecraft account. Type /link " + linkToken + " to accept", NotificationTypes.INFO);
    }

    public static void acceptLinkRequest(String linkToken, UUID uuid) {
        LinkRequest linkRequest = linkRequests.get(linkToken);
        linkRequests.remove(linkToken);
        LinkingAPI.linkAccounts(linkRequest.accessToken(), uuid);
    }

    public static void declineLinkRequest(String linkToken) {
        linkRequests.remove(linkToken);
    }
}
