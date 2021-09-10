package arnaria.kingdoms.mixin;

import arnaria.kingdoms.interfaces.BannerBlockInf;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBannerBlock.class)
public abstract class AbstractBannerBlockMixin extends BlockWithEntity implements BannerBlockInf {

    private boolean isClaimMarker = false;

    protected AbstractBannerBlockMixin(Settings settings) {
        super(settings);

    }

    public void setClaimMarker(boolean value) {
        this.isClaimMarker = value;
        StateManager.Builder<Block, BlockState> builder = new StateManager.Builder<>(this);
        BooleanProperty test = BooleanProperty.of("isClaimMarker");
        test.name(true);
        builder.add(test);

    }

    public boolean isClaimMarker() {
        return this.isClaimMarker;
    }

}
