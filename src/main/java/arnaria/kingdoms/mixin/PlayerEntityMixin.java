package arnaria.kingdoms.mixin;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityInf {

    private String kingdomId;
    private boolean isKing = false;

    public void setKingdomId(String kingdomId) {
        this.kingdomId = kingdomId;
    }

    public String getKingdomId() {
        return this.kingdomId;
    }

    public void setKingship(boolean isKing) {
        this.isKing = isKing;
    }

    public boolean isKing() {
        return isKing;
    }
}
