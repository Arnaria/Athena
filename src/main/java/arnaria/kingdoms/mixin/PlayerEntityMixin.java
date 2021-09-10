package arnaria.kingdoms.mixin;

import arnaria.kingdoms.callbacks.PlayerDeathCallback;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static arnaria.kingdoms.Kingdoms.playerManager;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityInf {

    private String kingdomId = "test";
    private boolean isKing = false;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

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

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource source, CallbackInfo ci) {
        PlayerEntity player = playerManager.getPlayer(this.getUuid());
        PlayerDeathCallback.EVENT.invoker().place(player, source);
    }
}