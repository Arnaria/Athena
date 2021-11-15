package arnaria.kingdoms.services.api;

import io.javalin.Javalin;

public class RestAPI {

    public static void init() {
        Javalin api = Javalin.create().start(8080);
        KingdomsAPI.init(api);
        LinkingAPI.init(api);
    }
}
