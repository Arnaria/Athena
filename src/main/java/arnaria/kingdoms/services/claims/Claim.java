package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.ClaimHelpers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.pb4.holograms.api.holograms.WorldHologram;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

import java.io.Serializable;
import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;
public class Claim implements Serializable {

    private String kingdomId;
    private String color;
    private final BlockPos pos;
    private final WorldHologram hologram;

    private final ArrayList<Chunk> claimChunks;
    private final ArrayList<ClaimEdge> edges = new ArrayList<>();

    public Claim(String kingdomId, BlockPos pos) {
        this.kingdomId = kingdomId;
        this.color = KingdomsData.getColor(kingdomId);
        this.pos = pos;
        this.claimChunks = ClaimHelpers.createChunkBox(pos, 5, true);
        this.hologram = new WorldHologram(overworld, new Vec3d(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5));

        LiteralText claimTag = new LiteralText(kingdomId.toUpperCase());
        claimTag.formatted(Formatting.byName(color));
        this.hologram.addText(claimTag);
        this.hologram.show();

        BlockPos[] corners = ClaimHelpers.getCorners(this.pos);
        int minX = corners[0].getX();
        int minZ = corners[0].getZ();
        int maxX = corners[1].getX();
        int maxZ = corners[1].getZ();

        for (int i = 0; i < 256; i++) {
            if (i % 2 == 0) {
                edges.add(new ClaimEdge(minX, i, minZ, minX, i, maxZ));
                edges.add(new ClaimEdge(maxX, i, minZ, maxX, i, maxZ));
                edges.add(new ClaimEdge(minX, i, minZ, maxX, i, minZ));
                edges.add(new ClaimEdge(minX, i, maxZ, maxX, i, maxZ));
            }
        }
    }

    public boolean isOverlapping(ArrayList<Chunk> testChunks) {
        for (Chunk chunk : this.claimChunks) {
            if (testChunks.contains(chunk)) return true;
        }
        return false;
    }

    public boolean contains(BlockPos pos) {
        return this.claimChunks.contains(overworld.getChunk(pos));
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

    public String getKingdomId() {
        return this.kingdomId;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getColor() {
        return this.color;
    }

    @JsonIgnore
    public ArrayList<ClaimEdge> getClaimEdges() {
        return this.edges;
    }
}