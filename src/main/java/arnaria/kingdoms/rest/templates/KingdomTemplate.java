package arnaria.kingdoms.rest.templates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import mrnavastar.sqlib.api.DataContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static arnaria.kingdoms.util.procedures.KingdomProcedures.kingdomData;
import static arnaria.kingdoms.Kingdoms.userCache;

public class KingdomTemplate implements Serializable {

    private final String kingdomId;
    private final String color;
    private final HashMap<String, UUID> king = new HashMap<>();
    private final HashMap<String, UUID> members = new HashMap<>();

    public KingdomTemplate(String id) {
        DataContainer dataContainer = kingdomData.get(id);
        this.kingdomId = id;
        this.color = dataContainer.getString("COLOR");

        UUID kingUuid = dataContainer.getUuid("KING");
        Optional<GameProfile> kingProfile = userCache.getByUuid(kingUuid);
        kingProfile.ifPresent(gameProfile -> this.king.put(gameProfile.getName(), kingUuid));

        JsonArray jsonArray = dataContainer.getJson("MEMBERS").getAsJsonArray();
        for (JsonElement member : jsonArray) {
            UUID uuid = UUID.fromString(member.getAsString());
            Optional<GameProfile> profile = userCache.getByUuid(uuid);
            profile.ifPresent(gameProfile -> this.members.put(gameProfile.getName(), uuid));
        }
    }

    public String getKingdomId() {
        return this.kingdomId;
    }

    public String getColor() {
        return color;
    }

    public HashMap<String, UUID> getKing() {
        return king;
    }

    public HashMap<String, UUID> getMembers() {
        return members;
    }
}
