package arnaria.kingdoms.util;

import arnaria.kingdoms.services.claims.ClaimManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;

public class ClaimHelpers {

    public static ArrayList<Chunk> createChunkBox(BlockPos center, int diameter, int startingOffset) {
        ArrayList<Chunk> chunkList = new ArrayList<>();
        int startX = center.getX() - startingOffset;
        int startZ = center.getZ() - startingOffset;

        int currentX = startX;
        int currentZ = startZ;
        for (int i = 0; i < diameter; i++) {
            for (int j = 0; j < diameter; j++) {
                BlockPos chunkPos = new BlockPos(currentX, 1, currentZ);
                if (ClaimManager.claimPlacementAllowedAt(chunkPos)) chunkList.add(overworld.getChunk(chunkPos));
                currentZ += 16;
            }
            currentX += 16;
            currentZ = startZ;
        }
        return chunkList;
    }
}
