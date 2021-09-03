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

    @Override
    public void setKingdomId(String kingdomId) {
        this.kingdomId = kingdomId;
    }

    @Override
    public String getKingdomId() {
        return this.kingdomId;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("KINGDOM_ID", this.kingdomId);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        this.kingdomId = nbt.getString("KINGDOM_ID");
    }
}
