package arnaria.kingdoms.util;

import arnaria.kingdoms.services.claims.ClaimManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;

import static arnaria.kingdoms.Kingdoms.overworld;

public class ClaimHelpers {

    public static ArrayList<Chunk> createChunkBox(BlockPos center, int diameter, boolean removeOverlapping) {
        ArrayList<Chunk> chunkList = new ArrayList<>();

        int startingOffset = (int) Math.floor((float) diameter / 2) * 16;
        int startX = center.getX() - startingOffset;
        int startZ = center.getZ() - startingOffset;

        int currentX = startX;
        int currentZ = startZ;
        for (int i = 0; i < diameter; i++) {
            for (int j = 0; j < diameter; j++) {
                BlockPos pos = new BlockPos(currentX, 1, currentZ);
                if (removeOverlapping && ClaimManager.claimPlacementAllowedAt(pos)) chunkList.add(overworld.getChunk(pos));
                if (!removeOverlapping) chunkList.add(overworld.getChunk(pos));
                currentZ += 16;
            }
            currentX += 16;
            currentZ = startZ;
        }
        return chunkList;
    }
}
