package corviolis.athena.services.api;

import corviolis.athena.services.procedures.LinkingProcedures;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;

public class LinkingAPI {

    private static final String url = "/v1/linking";

    public static void init(Javalin api) {
        api.post(url, ctx -> {
            JsonObject obj = (JsonObject) JsonParser.parseString(ctx.body());

            if (obj.get("action").getAsString().equals("link")) {
                JsonObject usernameData = obj.get("username_data").getAsJsonObject();
                String username = usernameData.get("username").getAsString();
                String type = usernameData.get("type").getAsString();

                if (type.equals("Java") | type.equals("Bedrock")) {
                    if (usernameData.get("type").getAsString().equals("Bedrock")) username = "BR_" + username;
                    LinkingProcedures.createLinkRequest(obj.get("access_token").getAsString(), obj.get("email").getAsString(), username);
                }
            }

            if (obj.get("action").getAsString().equals("unlink")) {
                LinkingProcedures.unlinkAccounts(obj.get("access_token").getAsString());
            }
        });
    }
}
