package arnaria.kingdoms.mixin;

import arnaria.kingdoms.services.claims.ClaimManager;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractDecorationEntity.class)
public abstract class AbstractDecorationEntityMixin extends Entity {

    public AbstractDecorationEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "canStayAttached", at = @At("HEAD"), cancellable = true)
    public void allowPlacement(CallbackInfoReturnable<Boolean> cir) throws CommandSyntaxException {
        if (!ClaimManager.actionAllowedAt(this.getBlockPos(), this.getCommandSource().getPlayer())) cir.setReturnValue(false);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void breakInClaim(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getAttacker();

        if (attacker instanceof PlayerEntity) {
            if (!ClaimManager.actionAllowedAt(this.getBlockPos(), (PlayerEntity) attacker)) cir.setReturnValue(false);
        }
    }
}
