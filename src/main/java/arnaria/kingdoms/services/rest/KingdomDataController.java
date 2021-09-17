package arnaria.kingdoms.services.rest;

import arnaria.kingdoms.services.claims.Claim;
import arnaria.kingdoms.services.claims.ClaimManager;
import arnaria.kingdoms.services.data.KingdomsData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.math.BlockPos;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static arnaria.kingdoms.Kingdoms.userCache;

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

        UUID king = KingdomsData.getKing(kingdomId);
        Optional<GameProfile> kingProfile = userCache.getByUuid(king);
        kingProfile.ifPresent(gameProfile -> mapping.put("kingUsername", gameProfile.getName()));
        mapping.put("kingUuid", king.toString());

        mapping.put("color", KingdomsData.getColor(kingdomId));
        mapping.put("claimMarkerPointsTotal", KingdomsData.getClaimMarkerPointsTotal(kingdomId));
        mapping.put("claimMarkerPointsUsed", KingdomsData.getClaimMarkerPointsUsed(kingdomId));
        return mapping;
    }

    @GetMapping(url + "/{kingdomId}/members")
    ObjectNode members(@PathVariable String kingdomId) {
        ObjectNode mapping = mapper.createObjectNode();

        ArrayList<String> memberUsernames = new ArrayList<>();
        ArrayList<UUID> memberUuids = new ArrayList<>();
        for (UUID member : KingdomsData.getMembers(kingdomId)) {
            Optional<GameProfile> memberProfile = userCache.getByUuid(member);
            memberProfile.ifPresent(gameProfile -> memberUsernames.add(gameProfile.getName()));
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
            Optional<GameProfile> requesterProfile = userCache.getByUuid(member);
            requesterProfile.ifPresent(gameProfile -> requesterUsernames.add(gameProfile.getName()));
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
            Optional<GameProfile> blockedProfile = userCache.getByUuid(member);
            blockedProfile.ifPresent(gameProfile -> blockedUsernames.add(gameProfile.getName()));
            blockedUuids.add(member);
        }

        mapping.putPOJO("memberUsernames", blockedUsernames);
        mapping.putPOJO("memberUuids", blockedUuids);
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