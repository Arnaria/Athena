package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.Kingdoms;
import arnaria.kingdoms.interfaces.BannerMarkerInf;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.BlueMapAPI;
import arnaria.kingdoms.util.ClaimHelpers;
import arnaria.kingdoms.util.Parser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.bluecolored.bluemap.api.marker.MarkerSet;
import de.bluecolored.bluemap.api.marker.Shape;
import de.bluecolored.bluemap.api.marker.ShapeMarker;
import mrnavastar.sqlib.api.DataContainer;
import mrnavastar.sqlib.api.Table;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;

import static arnaria.kingdoms.Kingdoms.overworld;
import static arnaria.kingdoms.Kingdoms.log;

public class ClaimManager {

    private static final Table claimData = Kingdoms.database.createTable("ClaimData");
    private static final ArrayList<Claim> claims = new ArrayList<>();
    private static final ArrayList<Chunk> adminClaim = new ArrayList<>();

    public static void init() {

        if (!claimData.contains("ADMIN_CLAIM")) {
            DataContainer adminClaimData = claimData.createDataContainer("ADMIN_CLAIM");
            adminClaimData.put("CHUNKS", new JsonArray());
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
                    adminClaim.add(overworld.getChunk(Parser.stringToBlockpos(strPos.getAsString())));
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

        Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);
        markerSet.ifPresent(markers -> {
            BlockPos[] corners = ClaimHelpers.getCorners(pos);
            ShapeMarker marker = markers.createShapeMarker(kingdomId + " : " + pos.toShortString(), BlueMapAPI.getOverworld(), corners[0].getX(), corners[0].getY(), corners[0].getZ(), Shape.createRect(corners[0].getX(), corners[0].getZ(), corners[1].getX(), corners[1].getZ()), pos.getY());
            Vec3f rgb = Parser.colorNameToRGB(KingdomsData.getColor(kingdomId));
            Color color = new Color(rgb.getX(), rgb.getY(), rgb.getZ(), 0.5F);
            marker.setColors(color, color.darker());
            BlueMapAPI.saveMarkers();
        });
    }

    public static void addAdminClaim(BlockPos pos) {
        adminClaim.add(overworld.getChunk(pos));

        DataContainer adminClaim = claimData.get("ADMIN_CLAIM");
        JsonArray chunks = (JsonArray) adminClaim.getJson("CHUNKS");
        chunks.add(pos.toShortString());
        adminClaim.put("CHUNKS", chunks);
    }

    private static void unlinkSouroundingClaims(Claim claim) {
        ArrayList<Chunk> chunks = ClaimHelpers.createChunkBox(claim.getPos(), 7, false);
        for (Claim c : claims) {
            if (c.isOverlapping(chunks)) c.unlink(claim);
        }
    }

    public static void dropClaim(BlockPos pos) {
        Claim claimToDrop = null;
        for (Claim claim : claims) {
            if (claim.getPos().equals(pos)) {
                unlinkSouroundingClaims(claim);
                claim.removeHologram();
                claimToDrop = claim;
                Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(claim.getKingdomId());
                markerSet.ifPresent(markers -> markers.removeMarker(claim.getKingdomId() + " : " + pos.toShortString()));
                KingdomProcedures.removeFromBannerCount(claim.getKingdomId(), 1);
                claimData.drop(pos.toShortString());
                break;
            }
        }

        if (claimToDrop != null) claims.remove(claimToDrop);
        BlueMapAPI.saveMarkers();
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
                Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);
                markerSet.ifPresent(markers -> markers.removeMarker(kingdomId + " : " + pos.toShortString()));
                if (overworld.getBlockState(pos).getBlock() instanceof BannerBlock) overworld.breakBlock(pos, false);
                claimData.drop(pos.toShortString());
            }
        }

        claimData.endTransaction();
        claims.removeAll(claimsToDrop);
        BlueMapAPI.saveMarkers();
    }

    public static void dropAdminClaim(BlockPos pos) {
        adminClaim.remove(overworld.getChunk(pos));

        DataContainer adminClaim = claimData.get("ADMIN_CLAIM");
        JsonArray chunks = (JsonArray) adminClaim.getJson("CHUNKS");

        int count = 0;
        for (JsonElement strPos : chunks) {
            if (strPos.getAsString().equals(pos.toShortString())) break;
            count++;
        }

        chunks.remove(count);
        adminClaim.put("CHUNKS", chunks);
    }

    public static ArrayList<Claim> getClaims() {
        return claims;
    }

    public static ArrayList<Claim> getClaims(String kingdomId) {
        ArrayList<Claim> kingdomClaims = new ArrayList<>();
        for (Claim claim : claims) if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) kingdomClaims.add(claim);
        return kingdomClaims;
    }

    public static void updateClaimColor(String kingdomId, String color) {
        for (Claim claim : claims) {
            if (claim.getKingdomId().equalsIgnoreCase(kingdomId)) {
                claim.updateColor(color);
                Optional<MarkerSet> markerSet = BlueMapAPI.getMarkerSet(kingdomId);
                markerSet.ifPresent(markers -> {
                    markers.removeMarker(kingdomId + " : " + claim.getPos().toShortString());

                    BlockPos pos = claim.getPos();
                    BlockPos[] corners = ClaimHelpers.getCorners(pos);
                    ShapeMarker marker = markers.createShapeMarker(kingdomId + " : " + claim.getPos().toShortString(), BlueMapAPI.getOverworld(), corners[0].getX(), corners[0].getY(), corners[0].getZ(), Shape.createRect(corners[0].getX(), corners[0].getZ(), corners[1].getX(), corners[1].getZ()), pos.getY());
                    Vec3f rgb = Parser.colorNameToRGB(KingdomsData.getColor(kingdomId));
                    Color c = new Color(rgb.getX(), rgb.getY(), rgb.getZ(), 0.5F);
                    marker.setColors(c, c.darker());
                });
                BlueMapAPI.saveMarkers();
            }
        }
    }

    public static boolean actionAllowedAt(BlockPos pos, PlayerEntity player) {
        if (pos.getY() < 0) return true;

        for (Claim claim : claims) {
            if (claim.contains(pos)) {
                if (!((PlayerEntityInf) player).allowedToEditIn(claim.getKingdomId())) return false;
            }
        }
        return !adminClaim.contains(overworld.getChunk(pos)) || player.hasPermissionLevel(4);
    }

    public static boolean canBreakClaim(BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.getPos().equals(pos) && claim.canBeBroken()) return true;
        }
        return false;
    }

    public static boolean claimExistsAt(BlockPos pos) {
        for (Claim claim : claims) {
            if (claim.contains(pos)) return true;
        }
        return adminClaim.contains(overworld.getChunk(pos));
    }

    public static boolean validBannerPos(String kingdomId, BlockPos pos) {
        if (adminClaim.contains(overworld.getChunk(pos))) return false;

        ArrayList<Chunk> chunks = ClaimHelpers.createChunkBox(pos, 7, false);
        for (Claim claim : claims) {
            if (claim.contains(pos)) return false;
            if (claim.getKingdomId().equals(kingdomId) && claim.isOverlapping(chunks)) return true;
        }
        return false;
    }

    public static boolean placedFirstBanner(String kingdomId) {
        return KingdomsData.getBannerCount(kingdomId) > 0;
    }

    public static boolean canAffordBanner(String kingdomId) {
        int bannersAllowed = (int) Math.floor((float) KingdomsData.getXp(kingdomId) / 1000) + 1;
        return bannersAllowed > KingdomsData.getBannerCount(kingdomId);
    }

    public static void renderClaims(ServerPlayerEntity player) {
        for (Claim claim : claims) ClaimHelpers.renderClaim(player, claim);
    }

    public static void renderClaimsForPlacement(ServerPlayerEntity player) {
        if (validBannerPos(((PlayerEntityInf) player).getKingdomId(), player.getBlockPos())) {
            ClaimHelpers.renderClaimLayer(player, player.getBlockPos(), (int) player.getY(), "white", 256 * 256);
        }
        for (Claim claim : claims) ClaimHelpers.renderClaimLayer(player, claim.getPos(), (int) player.getY(), claim.getColor(), 256 * 256);
    }
}