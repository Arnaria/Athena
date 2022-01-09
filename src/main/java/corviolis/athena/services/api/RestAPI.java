package corviolis.athena.services.api;

import corviolis.athena.Kingdoms;
import io.javalin.Javalin;

public class RestAPI {

    public static void init() {
        Javalin api = Javalin.create().start(Kingdoms.settings.API_PORT);
        NationsAPI.init(api);
        LinkingAPI.init(api);
    }
}
