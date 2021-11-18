package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.ClaimHelpers;
import eu.pb4.holograms.api.holograms.WorldHologram;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;

public class Claim {

    private String kingdomId;
    private String color;
    private final BlockPos pos;
    private final WorldHologram hologram;
    private final ArrayList<Chunk> chunks;
    private final ArrayList<BlockPos> linkedClaims = new ArrayList<>();
    private boolean isStartingClaim = false;

    public Claim(String kingdomId, BlockPos pos) {
        this.kingdomId = kingdomId;
        this.color = KingdomsData.getColor(kingdomId);
        this.pos = pos;
        this.chunks = ClaimHelpers.createChunkBox(pos, 5, true);
        this.hologram = new WorldHologram(overworld, new Vec3d(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5));

        if (!ClaimManager.placedFirstBanner(kingdomId)) isStartingClaim = true;

        ArrayList<Chunk> chunks = ClaimHelpers.createChunkBox(pos, 7, false);
        for (Claim claim : ClaimManager.getClaims()) {
            if (claim.isOverlapping(chunks)) link(claim);
        }

        LiteralText claimTag = new LiteralText(kingdomId.toUpperCase());
        Formatting formatting = Formatting.byName(color);
        if (formatting != null) claimTag.formatted(formatting);
        this.hologram.addText(claimTag);
        this.hologram.show();
    }

    public boolean isOverlapping(ArrayList<Chunk> testChunks) {
        for (Chunk chunk : this.chunks) {
            if (testChunks.contains(chunk)) return true;
        }
        return false;
    }

    public boolean contains(BlockPos pos) {
        return this.chunks.contains(overworld.getChunk(pos));
    }

    public boolean canBeBroken() {
        for (Claim claim : ClaimManager.getClaims()) {
            if (linkedClaims.contains(claim.getPos()) && !(claim.getLinkedClaims().size() > 1)) return false;
        }
        if (isStartingClaim && KingdomsData.getBannerCount(kingdomId) > 1) return false;
        return true;
    }

    public void updateColor(String color) {
        this.color = color;
        this.hologram.removeElement(0);
        this.hologram.addText(new LiteralText(kingdomId.toUpperCase()).formatted(Formatting.byName(color)));
    }

    public void rebrand(String kingdomId, String color) {
        this.kingdomId = kingdomId;
        updateColor(color);
    }

    public void removeHologram() {
        this.hologram.hide();
    }

    public void link(Claim claim) {
        linkedClaims.add(claim.getPos());
        claim.linkedClaims.add(this.getPos());
    }

    public void unlink(Claim claim) {
        linkedClaims.remove(claim.getPos());
        claim.linkedClaims.remove(this.pos);
    }

    public ArrayList<BlockPos> getLinkedClaims() {
        return linkedClaims;
    }

    public String getKingdomId() {
        return this.kingdomId;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getColor() {
        return this.color;
    }
}