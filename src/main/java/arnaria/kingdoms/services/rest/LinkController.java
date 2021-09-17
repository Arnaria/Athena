package arnaria.kingdoms.services.rest;

import arnaria.kingdoms.services.procedures.LinkProcedures;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojang.authlib.GameProfile;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import static arnaria.kingdoms.Kingdoms.userCache;

@RestController
public class LinkController {

    private static final String url = "/api/linking";
    private static final ObjectMapper mapper = new ObjectMapper();

    @GetMapping(url + "/token")
    String linkToken() {
        return LinkProcedures.getValidLinkToken();
    }

    @PostMapping(url + "/request")
    void createLinkRequest(@RequestParam String userToken, @RequestParam String linkToken, @RequestParam String uuid) {
        LinkProcedures.createLinkRequest(userToken, linkToken, UUID.fromString(uuid));
    }

    @PostMapping(url + "/account")
    ObjectNode mcAccount(@RequestParam String userToken) {
        ObjectNode mapping = mapper.createObjectNode();

        UUID uuid = LinkProcedures.getAccount(userToken);
        Optional<GameProfile> kingProfile = userCache.getByUuid(uuid);
        kingProfile.ifPresent(gameProfile -> mapping.put("username", gameProfile.getName()));
        mapping.put("uuid", uuid.toString());
        return mapping;
    }
}
