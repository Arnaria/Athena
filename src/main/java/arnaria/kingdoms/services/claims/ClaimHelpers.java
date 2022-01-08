package arnaria.kingdoms.services.claims;

import net.minecraft.util.math.*;

import java.util.ArrayList;

public class ClaimHelpers {

    public static ArrayList<ChunkPos> createChunkBox(BlockPos center, int diameter, boolean removeOverlapping) {
        ArrayList<ChunkPos> chunks = new ArrayList<>();

        int startingOffset = (int) Math.floor((float) diameter / 2) * 16;
        int startX = center.getX() - startingOffset;
        int startZ = center.getZ() - startingOffset;

        int currentX = startX;
        for (int i = 0; i < diameter; i++) {
            int currentZ = startZ;
            for (int j = 0; j < diameter; j++) {
                ChunkPos pos = new ChunkPos(new BlockPos(currentX, 0, currentZ));
                if (removeOverlapping && !ClaimManager.claimExistsAt(pos)) chunks.add(pos);
                if (!removeOverlapping) chunks.add(pos);
                currentZ += 16;
            }
            currentX += 16;
        }
        return chunks;
    }
}