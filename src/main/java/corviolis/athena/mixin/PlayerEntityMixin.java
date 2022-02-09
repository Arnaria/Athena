package corviolis.athena.mixin;

import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.claims.ClaimRenderer;
import corviolis.athena.services.claims.ClaimManager;
import corviolis.athena.services.events.Event;
import corviolis.athena.services.events.EventManager;
import corviolis.athena.util.BetterPlayerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements PlayerEntityInf {

    @Shadow public abstract Iterable<ItemStack> getItemsHand();

    private String kingdomId = "";
    private boolean isKing = false;
    private final ArrayList<String> allowedToEditIn = new ArrayList<>();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    public void setKingdomId(String kingdomId) {
        if (kingdomId.isEmpty()) this.allowedToEditIn.remove(this.kingdomId);
        else this.allowedToEditIn.add(kingdomId);
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

    public void allowToEditIn(String kingdomId) {
        this.allowedToEditIn.add(kingdomId);
    }

    public void removeAllowedToEditIn(String kingdomId) {
        this.allowedToEditIn.remove(kingdomId);
    }

    public boolean allowedToEditIn(String kingdomId) {
        return this.allowedToEditIn.contains(kingdomId);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity player = BetterPlayerManager.getPlayer(this.uuid);
        Event event = EventManager.getEvent(player);

        if (source.getAttacker() instanceof ServerPlayerEntity killer && event != null) {
            event.onDeath(player, killer);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void stopAttackInClaims(Entity target, CallbackInfo ci) {
        ServerPlayerEntity player = BetterPlayerManager.getPlayer(this.uuid);
        if ((target instanceof HostileEntity) && ClaimManager.actionAllowedAt(target.getBlockPos(), player)) ci.cancel();
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ServerPlayerEntity player = BetterPlayerManager.getPlayer(this.uuid);
        ItemStack stack = this.getItemsHand().iterator().next();
        if (stack.getItem() instanceof BannerItem) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.getBoolean("IS_CLAIM_MARKER")) ClaimRenderer.renderForPlacement(player);
        } else ClaimRenderer.render(BetterPlayerManager.getPlayer(this.uuid));
    }
}