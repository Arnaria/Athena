package arnaria.kingdoms.util.claims;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;

import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.chunkManager;

public class Claim {

    private final String kingdomId;
    private final ArrayList<Chunk> chunks = new ArrayList<>();

    public Claim(String kingdomId, BlockPos pos) {
        this.kingdomId = kingdomId;
    }

    public boolean contains(BlockPos pos) {
        return chunks.contains(chunkManager.getWorldChunk(pos.getX(), pos.getZ()));
    }
}
