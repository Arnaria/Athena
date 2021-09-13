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

    private final String kingdomId;
    private final BlockPos pos;
    private final WorldHologram hologram;

    private final ArrayList<Chunk> claimChunks;

    public Claim(String kingdomId, BlockPos pos) {
        this.kingdomId = kingdomId;
        this.pos = pos;
        this.claimChunks = ClaimHelpers.createChunkBox(pos, 5, true);

        this.hologram = new WorldHologram(overworld, new Vec3d(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5));

        LiteralText claimTag = new LiteralText(kingdomId.toUpperCase());
        String color = KingdomsData.getColor(kingdomId);

        if (!color.isEmpty()) claimTag.formatted(Formatting.byName(color));
        this.hologram.addText(claimTag);
        this.hologram.show();
    }

    public void updateColor(String color) {
        this.hologram.removeElement(0);
        this.hologram.addText(new LiteralText(kingdomId.toUpperCase()).formatted(Formatting.byName(color)));
    }

    public void removeHologram() {
        this.hologram.hide();
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

    public String getKingdomId() {
        return this.kingdomId;
    }

    public BlockPos getPos() {
        return pos;
    }
}
