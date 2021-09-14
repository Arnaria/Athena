package arnaria.kingdoms.mixin;

import arnaria.kingdoms.interfaces.BannerMarkerInf;
import net.minecraft.item.BannerItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BannerItem.class)
public class BannerItemMixin implements BannerMarkerInf {

    private boolean isClaimMarker = false;

    public void makeClaimMarker() {
        this.isClaimMarker = true;
    }

    public boolean isClaimMarker() {
        return this.isClaimMarker;
    }
}
