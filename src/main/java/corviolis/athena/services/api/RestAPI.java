package corviolis.athena.services.api;

import corviolis.athena.Athena;
import io.javalin.Javalin;

public class RestAPI {

    public static void init() {
        Javalin api = Javalin.create().start(Athena.settings.API_PORT);
        //NationsAPI.init(api);
        //LinkingAPI.init(api);
        LeaderboardAPI.init(api);
    }
}
