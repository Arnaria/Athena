package corviolis.athena.services.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import corviolis.athena.services.data.KingdomsData;

import java.io.IOException;
import java.util.Objects;

public class LeaderboardHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("GET")) {
            JsonArray kingdoms = new JsonArray();
            for (String kingdomId :  KingdomsData.getKingdomIds()) {
                if (!Objects.equals(kingdomId, "ADMIN")) {
                    JsonObject kingdom = new JsonObject();
                    kingdom.addProperty("kingdomId", kingdomId);
                    kingdom.addProperty("xp", KingdomsData.getXp(kingdomId));
                    kingdom.addProperty("color", KingdomsData.getColor(kingdomId));
                    kingdoms.add(kingdom);
                }
            }

            String response = kingdoms.toString();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
    }
}