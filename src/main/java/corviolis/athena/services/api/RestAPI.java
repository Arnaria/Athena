package corviolis.athena.services.api;

import com.sun.net.httpserver.HttpServer;
import corviolis.athena.Athena;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RestAPI {
    public static void init() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(Athena.settings.API_PORT), 0);
            server.createContext("/leaderboard", new LeaderboardHttpHandler());
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            Athena.log(Level.INFO, "Internal http server started");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}