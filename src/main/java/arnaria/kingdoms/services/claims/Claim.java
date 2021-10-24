package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.util.ClaimHelpers;
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

    private final ArrayList<Chunk> chunks;

    public Claim(String kingdomId, BlockPos pos) {
        this.kingdomId = kingdomId;
        this.color = KingdomsData.getColor(kingdomId);
        this.pos = pos;
        this.chunks = ClaimHelpers.createChunkBox(pos, 5, true);
        this.hologram = new WorldHologram(overworld, new Vec3d(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5));

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
}