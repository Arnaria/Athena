package arnaria.kingdoms.services.api;

import arnaria.kingdoms.Kingdoms;
import io.javalin.Javalin;

public class RestAPI {

    public static void init() {
        Javalin api = Javalin.create().start(Kingdoms.settings.API_PORT);
        KingdomsAPI.init(api);
        LinkingAPI.init(api);
    }
}
