package arnaria.kingdoms.services.events;

import arnaria.kingdoms.callbacks.PlayerDeathCallback;
import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.claims.ClaimManager;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BannerBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

import static arnaria.kingdoms.Kingdoms.playerManager;
import static arnaria.kingdoms.Kingdoms.settings;

public class InvasionEvent extends Event {

    private final String defendingKingdomId;
    private final String attackingKingdomId;
    private int defendingKills = 0;
    private int attackingKills = 0;
    private boolean bannerBroken = false;

    public InvasionEvent(String defendingKingdomId, String attackingKingdomId, BlockPos bannerPos) {
        super(settings.INVASION_DURATION, "INVASION");
        this.defendingKingdomId = defendingKingdomId;
        this.attackingKingdomId = attackingKingdomId;

        for (UUID member : KingdomsData.getMembers(defendingKingdomId)) {
            ServerPlayerEntity player = playerManager.getPlayer(member);
            if (player != null) this.addPlayer(player);
        }

        for (UUID member : KingdomsData.getMembers(attackingKingdomId)) {
            ServerPlayerEntity player = playerManager.getPlayer(member);
            if (player != null) this.addPlayer(player);
        }

        PlayerDeathCallback.EVENT.register(((player, damageSource) -> {
            Entity killer = damageSource.getSource();
            if (killer instanceof PlayerEntity && (((PlayerEntityInf) killer).getKingdomId().equalsIgnoreCase(attackingKingdomId))) attackingKills++;
            if (killer instanceof PlayerEntity && (((PlayerEntityInf) killer).getKingdomId().equalsIgnoreCase(defendingKingdomId))) defendingKills++;
            return ActionResult.PASS;
        }));

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (state.getBlock() instanceof BannerBlock && pos.equals(bannerPos)) {
                ClaimManager.dropClaim(bannerPos);
                this.bannerBroken = true;
                this.stopEvent();
            }
        });
    }

    @Override
    public void finish() {
        if (bannerBroken) {
            for (PlayerEntity member : this.getMembers()) {
                String type = "";
                if (((PlayerEntityInf) member).getKingdomId().equalsIgnoreCase(defendingKingdomId)) type = NotificationTypes.WARN;
                if (((PlayerEntityInf) member).getKingdomId().equalsIgnoreCase(attackingKingdomId)) type = NotificationTypes.ACHIEVEMENT;
                NotificationManager.send(member.getUuid(), attackingKingdomId + " has won the invasion!", type);
            }
        } else {
            for (PlayerEntity member : this.getMembers()) {
                String type = "";
                if (((PlayerEntityInf) member).getKingdomId().equalsIgnoreCase(defendingKingdomId)) type = NotificationTypes.ACHIEVEMENT;
                if (((PlayerEntityInf) member).getKingdomId().equalsIgnoreCase(attackingKingdomId)) type = NotificationTypes.WARN;
                NotificationManager.send(member.getUuid(), defendingKingdomId + " has won against the invasion!", type);
            }
        }
    }
}
