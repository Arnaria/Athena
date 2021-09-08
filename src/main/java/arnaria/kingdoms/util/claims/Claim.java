package arnaria.kingdoms.util.claims;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.io.Serializable;
import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.chunkManager;

public class Claim implements Serializable {

    private final String kingdomId;
    private final BlockPos pos;
    private final ArrayList<Chunk> chunks = new ArrayList<>();

    public Claim(String kingdomId, BlockPos pos) {
        this.kingdomId = kingdomId;
        this.pos = pos;

        int startX = pos.getX() - 32;
        int startZ = pos.getZ() + 32;

        int currentX = startX;
        int currentZ = startZ;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (ClaimManager.claimPlacementAllowedAt(currentX, currentZ)) {
                    chunks.add(chunkManager.getChunk(currentX, currentZ, ChunkStatus.SURFACE, false));
                }
                currentZ += 16;
            }
            currentX += 16;
            currentZ = startZ;
        }
    }

    public boolean contains(BlockPos pos) {
        return chunks.contains(chunkManager.getChunk(pos.getX(), pos.getZ(), ChunkStatus.SURFACE, false));
    }

    public String getKingdomId() {
        return this.kingdomId;
    }

    public BlockPos getPos() {
        return pos;
    }
}
