package arnaria.kingdoms.services.api;

import arnaria.kingdoms.services.procedures.LinkingProcedures;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import java.util.UUID;

import static arnaria.kingdoms.Kingdoms.settings;

public class LinkingAPI {

    private static final String url = "/v1/linking";

    public static void init(Javalin api) {
        api.post(url, ctx -> {
            JsonObject obj = (JsonObject) JsonParser.parseString(ctx.body());
            JsonObject usernameData = obj.get("username_data").getAsJsonObject();
            String username = usernameData.get("username").getAsString();
            String type = usernameData.get("type").getAsString();

            if (type.equals("Java") | type.equals("Bedrock")) {
                if (usernameData.get("type").getAsString().equals("Bedrock")) username = "BR_" + username;
                LinkingProcedures.createLinkRequest(obj.get("access_token").getAsString(), obj.get("email").getAsString(), username);
            }
        });
    }

    private static String getAuth0AccessToken() {
        HttpResponse<String> response = Unirest.post("https://dev-av6na9sy.us.auth0.com/oauth/token")
                .header("content-type", "application/json")
                .body("{\"client_id\":\"" + settings.CLIENT_ID + "\",\"client_secret\":\"" + settings.CLIENT_SECRET + "\",\"audience\":\"https://dev-av6na9sy.us.auth0.com/api/v2/\",\"grant_type\":\"client_credentials\"}")
                .asString();

        return JsonParser.parseString(response.getBody()).getAsJsonObject().get("access_token").getAsString();
    }

    public static void linkAccounts(String accessToken, UUID uuid) {
         HttpResponse<String> response = Unirest.patch("https://dev-av6na9sy.us.auth0.com/api/v2/users/" + accessToken.replaceAll("\\|", "%7C"))
                .header("authorization", "Bearer " + getAuth0AccessToken())
                .header("content-type", "application/json")
                .body("{\"app_metadata\": {\"uuid\": \"" + uuid + "\"}}")
                .asString();
    }
}
