package arnaria.kingdoms.services.rest.templates;

import arnaria.kingdoms.services.data.KingdomsData;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import mrnavastar.sqlib.api.DataContainer;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static arnaria.kingdoms.services.procedures.KingdomProcedures.kingdomData;
import static arnaria.kingdoms.Kingdoms.userCache;

public class KingdomTemplate implements Serializable {

    private final String kingdomId;
    private final String color;
    private final HashMap<String, UUID> king = new HashMap<>();
    private final HashMap<String, UUID> members = new HashMap<>();

    public KingdomTemplate(String id) {
        this.kingdomId = id;
        this.color = KingdomsData.getColor(id);

        UUID king = KingdomsData.getKing(id);
        Optional<GameProfile> kingProfile = userCache.getByUuid(king);
        kingProfile.ifPresent(gameProfile -> this.king.put(gameProfile.getName(), king));

        for (UUID member : KingdomsData.getMembers(id)) {
            Optional<GameProfile> memberProfile = userCache.getByUuid(member);
            memberProfile.ifPresent(gameProfile -> this.king.put(gameProfile.getName(), member));
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
