package arnaria.kingdoms.services.claims;

import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.kingdoms.util.ClaimHelpers;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.io.Serializable;
import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;
public class Claim implements Serializable {

    private final String kingdomId;
    private final BlockPos pos;

    private final ArrayList<Chunk> claimChunks;

    public Claim(String kingdomId, BlockPos pos) {
        this.kingdomId = kingdomId;
        this.pos = pos;
        this.claimChunks = ClaimHelpers.createChunkBox(pos, 5, true);
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
