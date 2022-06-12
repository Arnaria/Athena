package corviolis.athena.mixin;

import com.mojang.authlib.GameProfile;
import corviolis.athena.interfaces.ServerPlayerEntityInf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityInf {

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Shadow public abstract boolean changeGameMode(GameMode gameMode);

    @Shadow public abstract void setCameraEntity(@Nullable net.minecraft.entity.Entity entity);


    @Shadow public abstract Entity getCameraEntity();

    public void trackEntity(Entity entity) {
        changeGameMode(GameMode.SPECTATOR);
        setCameraEntity(entity);
    }

    public void stopTrackingEntity() {
        setCameraEntity(null);
    }

/*    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        Entity trackedEntity = getCameraEntity();
        if (trackedEntity != null && !getCameraBlockPos().equals(trackedEntity.getC)) {
            setCameraEntity(trackedEntity);
        }
    }*/
}
