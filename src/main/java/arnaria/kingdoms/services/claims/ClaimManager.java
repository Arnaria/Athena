package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.interfaces.BannerMarkerInf;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.ClaimHelpers;
import arnaria.kingdoms.util.Parser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.explosion.Explosion;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;
import static arnaria.kingdoms.Kingdoms.database;
import static arnaria.kingdoms.Kingdoms.log;

public class ClaimManager {

    private static final Table claimData = database.createTable("ClaimData");
    private static final ArrayList<Claim> claims = new ArrayList<>();
    private static final ArrayList<Chunk> adminClaims = new ArrayList<>();

    public static void init() {

        if (!claimData.contains("ADMIN_CLAIM")) {
            DataContainer adminClaim = claimData.createDataContainer("ADMIN_CLAIM");
            adminClaim.put("CHUNKS", new JsonArray());
        }

        for (DataContainer claim : claimData.getDataContainers()) {
            if (!claim.getId().equals("ADMIN_CLAIM")) {
                String kingdomId = claim.getString("KINGDOM_ID");
                BlockPos pos = claim.getBlockPos("BANNER_POS");
                claims.add(new Claim(kingdomId, pos));

                Block block = overworld.getBlockState(pos).getBlock();
                if (block instanceof BannerBlock bannerBlock) ((BannerMarkerInf) bannerBlock).makeClaimMarker();
                else {
                    log(Level.ERROR, "Mismatch in claim data and banner pos!");
                    log(Level.ERROR, "The error occurred at: " + pos + ". Suspected BannerBlock, got " + block + ".");
                    log(Level.ERROR, "This could have been caused by the server shutting down improperly");
                }
            } else {
                for (JsonElement strPos : claim.getJson("CHUNKS").getAsJsonArray()) {
                    adminClaims.add(overworld.getChunk(Parser.blockPosFromString(strPos.getAsString())));
                }
            }
        }

        ClaimEvents.register();
    }

    public static void addClaim(String kingdomId, BlockPos pos, BannerBlock banner) {
        Claim claim = new Claim(kingdomId, pos);
        claims.add(claim);

        claimData.beginTransaction();
        DataContainer claimDataContainer = claimData.createDataContainer(claim.getPos().toShortString());
        claimDataContainer.put("KINGDOM_ID", claim.getKingdomId());
        claimDataContainer.put("BANNER_POS", claim.getPos());
        claimData.endTransaction();

        ((BannerMarkerInf) banner).makeClaimMarker();
        KingdomProcedures.addToBannerCount(kingdomId, 1);
        if (!placedFirstBanner(kingdomId)) KingdomProcedures.setStartingBannerPos(kingdomId, pos);
    }

    public static void addAdminClaim(BlockPos pos) {
        adminClaims.add(overworld.getChunk(pos));

        DataContainer adminClaim = claimData.get("ADMIN_CLAIM");
        JsonArray chunks = (JsonArray) adminClaim.getJson("CHUNKS");
        chunks.add(pos.toShortString());
        adminClaim.put("CHUNKS", chunks);
    }

    public static void dropClaim(BlockPos pos) {
        Claim claimToDrop = null;
        for (Claim claim : claims) {
            if (claim.getPos().equals(pos)) {
                claim.removeHologram();
                claimToDrop = claim;
                claimData.drop(pos.toShortString());
                KingdomProcedures.removeFromBannerCount(claim.getKingdomId(), 1);
                break;
            }
        }

        if (claimToDrop != null) claims.remove(claimToDrop);
    }

    //Only use when dropping kingdoms
    public static void dropClaims(String kingdomId) {
        claimData.beginTransaction();
        ArrayList<Claim> claimsToDrop = new ArrayList<>();
        for (Claim claim : claims) {
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) {
                BlockPos pos = claim.getPos();
                claim.removeHologram();
                claimsToDrop.add(claim);
                claimData.drop(pos.toShortString());
                if (overworld.getBlockState(pos).getBlock() instanceof BannerBlock) overworld.breakBlock(pos, false);
            }
        }
        claimData.endTransaction();
        claims.removeAll(claimsToDrop);
    }

    public static void dropAdminClaim(BlockPos pos) {
        adminClaims.remove(overworld.getChunk(pos));

        DataContainer adminClaim = claimData.get("ADMIN_CLAIM");
        JsonArray chunks = (JsonArray) adminClaim.getJson("CHUNKS");

        int count = 0;
        for (JsonElement strPos : chunks) {
            if (strPos.toString().equals(pos.toShortString())) {
                chunks.remove(count);
                adminClaim.put("CHUNKS", chunks);
                break;
            }
            count++;
        }
    }

    public static ArrayList<Claim> getClaims() {
        return claims;
    }

    public static ArrayList<Claim> getClaims(String kingdomId) {
        ArrayList<Claim> kingdomClaims = new ArrayList<>();
        for (Claim claim : claims) if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) kingdomClaims.add(claim);
        return kingdomClaims;
    }

    public static void updateClaimTagColor(String kingdomId, String color) {
        for (Claim claim : claims) {
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) claim.updateColor(color);
        }
    }

    public static boolean actionAllowedAt(BlockPos pos, PlayerEntity player) {
        if (pos.getY() < 0) return true;

        for (Claim claim : claims) {
            if (claim.contains(pos)) {
                if (!claim.getKingdomId().equals(((PlayerEntityInf) player).getKingdomId())) return false;
            }
        }

        return !adminClaims.contains(overworld.getChunk(pos)) || player.hasPermissionLevel(4);
    }

    public static boolean claimExistsAt(BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.contains(pos)) return true;
        }

        return adminClaims.contains(overworld.getChunk(pos));
    }

    public static boolean validBannerPos(String kingdomId, BlockPos pos) {
        ArrayList<Chunk> chunks = ClaimHelpers.createChunkBox(pos, 7, false);
        for (Claim claim : claims) {
            if (claim.contains(pos)) return false;
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId) && !claim.isOverlapping(chunks)) return false;
        }

        return !adminClaims.contains(overworld.getChunk(pos));
    }

    public static boolean placedFirstBanner(String kingdomId) {
        return KingdomsData.getBannerCount(kingdomId) > 0;
    }

    public static void renderClaims(ServerPlayerEntity player) {
        for (Claim claim : claims) ClaimHelpers.renderClaim(player, claim);
    }

    public static void renderClaimsForPlacement(ServerPlayerEntity player) {
        if (validBannerPos(((PlayerEntityInf) player).getKingdomId(), player.getBlockPos())) {
            ClaimHelpers.renderClaimLayer(player, player.getBlockPos(), (int) player.getY(), Formatting.WHITE, 256 * 256);
        }
        for (Claim claim : claims) {
            ClaimHelpers.renderClaimLayer(player, claim.getPos(), (int) player.getY(), Formatting.byName(claim.getColor()), 256 * 256);
        }
    }

    public static boolean canAffordBanner(String kingdomId) {
        int bannersAllowed = (int) Math.floor((float) KingdomsData.getXp(kingdomId) / 1000) + 1;
        return bannersAllowed > KingdomsData.getBannerCount(kingdomId);
    }
}