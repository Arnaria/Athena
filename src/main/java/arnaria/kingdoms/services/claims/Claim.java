package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.ClaimHelpers;
import eu.pb4.holograms.api.holograms.WorldHologram;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;

public class Claim {

    private String kingdomId;
    private String color;
    private final BlockPos pos;
    private final WorldHologram hologram;
    private final ArrayList<ChunkPos> chunks;
    private final ArrayList<BlockPos> linkedClaims = new ArrayList<>();

    public Claim(String kingdomId, BlockPos pos) {
        this.kingdomId = kingdomId;
        this.color = KingdomsData.getColor(kingdomId);
        this.pos = pos;
        this.chunks = ClaimHelpers.createChunkBox(pos, 5, true);
        this.hologram = new WorldHologram(overworld, new Vec3d(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5));

        ArrayList<ChunkPos> chunks = ClaimHelpers.createChunkBox(pos, 7, false);
        for (Claim claim : ClaimManager.getClaims()) {
            if (claim.isOverlapping(chunks)) link(claim);
        }

        LiteralText claimTag = new LiteralText(kingdomId.toUpperCase());
        Formatting formatting = Formatting.byName(color);
        if (formatting != null) claimTag.formatted(formatting);
        this.hologram.addText(claimTag);
        this.hologram.show();
    }

    public boolean isOverlapping(ArrayList<ChunkPos> testChunks) {
        for (ChunkPos chunk : this.chunks) {
            if (testChunks.contains(chunk)) return true;
        }
        return false;
    }

    public boolean contains(BlockPos pos) {
        return this.chunks.contains(overworld.getChunk(pos).getPos());
    }

    public boolean canBeBroken() {
        BlockPos startingClaimPos = KingdomsData.getStartingClaimPos(kingdomId);
        if (pos.equals(startingClaimPos) && KingdomsData.getBannerCount(kingdomId) == 1) return true;

        for (Claim claim : ClaimManager.getClaims()) {
            if (linkedClaims.contains(claim.pos) && !(claim.linkedClaims.size() > 1) && !claim.pos.equals(startingClaimPos)) return false;
        }
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
        linkedClaims.add(claim.pos);
        claim.linkedClaims.add(this.pos);
    }

    public void unlink(Claim claim) {
        linkedClaims.remove(claim.pos);
        claim.linkedClaims.remove(this.pos);
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