package arnaria.kingdoms.services.rest;

import arnaria.kingdoms.services.claims.Claim;
import arnaria.kingdoms.services.claims.ClaimManager;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.BetterPlayerManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.minecraft.util.math.BlockPos;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class KingdomDataController {

    private static final String url = "/api/kingdoms";
    private static final ObjectMapper mapper = new ObjectMapper();

    @GetMapping(url)
    ArrayList<String> kingdoms() {
        return KingdomsData.getKingdomIds();
    }

    @GetMapping(url + "/{kingdomId}")
    ObjectNode kingdom(@PathVariable String kingdomId) {
        ObjectNode mapping = mapper.createObjectNode();

        mapping.put("kingUuid", KingdomsData.getKing(kingdomId).toString());

        mapping.put("color", KingdomsData.getColor(kingdomId));
        mapping.put("xp", KingdomsData.getXp(kingdomId));
        mapping.put("bannerCount", KingdomsData.getBannerCount(kingdomId));
        mapping.putPOJO("startingBannerPos", KingdomsData.getStartingBannerPos(kingdomId));
        return mapping;
    }

    @GetMapping(url + "/{kingdomId}/members")
    ObjectNode members(@PathVariable String kingdomId) {
        ObjectNode mapping = mapper.createObjectNode();

        ArrayList<String> memberUsernames = new ArrayList<>();
        ArrayList<UUID> memberUuids = new ArrayList<>();
        for (UUID member : KingdomsData.getMembers(kingdomId)) {
            memberUsernames.add(BetterPlayerManager.getName(member));
            memberUuids.add(member);
        }

        mapping.putPOJO("memberUsernames", memberUsernames);
        mapping.putPOJO("memberUuids", memberUuids);
        return mapping;
    }

    @GetMapping(url + "/{kingdomId}/requests")
    ObjectNode requests(@PathVariable String kingdomId) {
        ObjectNode mapping = mapper.createObjectNode();

        ArrayList<String> requesterUsernames = new ArrayList<>();
        ArrayList<UUID> requesterUuids = new ArrayList<>();
        for (UUID member : KingdomsData.getJoinRequests(kingdomId)) {
            requesterUsernames.add(BetterPlayerManager.getName(member));
            requesterUuids.add(member);
        }

        mapping.putPOJO("memberUsernames", requesterUsernames);
        mapping.putPOJO("memberUuids", requesterUuids);
        return mapping;
    }

    @GetMapping(url + "/{kingdomId}/blocked")
    ObjectNode blocked(@PathVariable String kingdomId) {
        ObjectNode mapping = mapper.createObjectNode();

        ArrayList<String> blockedUsernames = new ArrayList<>();
        ArrayList<UUID> blockedUuids = new ArrayList<>();
        for (UUID member : KingdomsData.getBlockedPlayers(kingdomId)) {
            blockedUsernames.add(BetterPlayerManager.getName(member));
            blockedUuids.add(member);
        }

        mapping.putPOJO("blockedUsernames", blockedUsernames);
        mapping.putPOJO("blockedUuids", blockedUuids);
        return mapping;
    }

    @GetMapping(url + "/claims")
    ArrayList<Claim> claims() {
        return ClaimManager.getClaims();
    }

    @GetMapping(url + "/{kingdomId}/claims")
    ArrayList<BlockPos> kingdomClaims(@PathVariable String kingdomId) {
        ArrayList<BlockPos> claims = new ArrayList<>();
        for (Claim claim : ClaimManager.getClaims(kingdomId)) claims.add(claim.getPos());
        return claims;
    }
}