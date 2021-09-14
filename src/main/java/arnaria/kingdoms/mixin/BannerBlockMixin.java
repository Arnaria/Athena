package arnaria.kingdoms.mixin;

import arnaria.kingdoms.interfaces.BannerBlockInf;
import net.minecraft.block.BannerBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BannerBlock.class)
public class BannerBlockMixin implements BannerBlockInf {

    private boolean isClaimMarker = false;

    public void makeClaimMarker() {
        this.isClaimMarker = true;
    }

    public boolean isClaimMarker() {
        return this.isClaimMarker;
    }
}
