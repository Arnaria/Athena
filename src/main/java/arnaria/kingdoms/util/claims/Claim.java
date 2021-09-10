package arnaria.kingdoms.util.claims;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.io.Serializable;
import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.worlds;
public class Claim implements Serializable {

    private final String kingdomId;
    private final BlockPos pos;
    private ServerWorld world;

    private final ArrayList<Chunk> chunks = new ArrayList<>();

    public Claim(String kingdomId, BlockPos pos) {
        this.kingdomId = kingdomId;
        this.pos = pos;

        for (ServerWorld world : worlds) {
            if (world.getRegistryKey().toString().equals("overworld")) this.world = world;
        }

        int startX = pos.getX() - 32;
        int startZ = pos.getZ() + 32;

        int currentX = startX;
        int currentZ = startZ;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                BlockPos chunkPos = new BlockPos(currentX, 1, currentZ);
                if (ClaimManager.claimPlacementAllowedAt(chunkPos)) this.chunks.add(world.getChunk(chunkPos));
                currentZ += 16;
            }
            currentX += 16;
            currentZ = startZ;
        }
    }

    public boolean contains(BlockPos pos) {
        return this.chunks.contains(world.getChunk(pos));
    }

    public String getKingdomId() {
        return this.kingdomId;
    }

    public BlockPos getPos() {
        return pos;
    }
}
