package arnaria.kingdoms.services.events;

import arnaria.kingdoms.callbacks.PlayerDeathCallback;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.notifacaitonmanager.NotificationManager;
import arnaria.notifacaitonmanager.NotificationTypes;
import com.google.gson.JsonElement;
import mrnavastar.sqlib.api.DataContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;

import java.util.UUID;

import static arnaria.kingdoms.services.procedures.KingdomProcedures.kingdomData;
import static arnaria.kingdoms.Kingdoms.playerManager;

public class RevolutionEvent extends Event {

    private final ServerBossBar bossBar;
    private final DataContainer kingdom;

    private int minPassed = 0;

    public RevolutionEvent(String kingdomId) {
        super(15);

        this.kingdom = kingdomData.get(kingdomId);
        this.bossBar = new ServerBossBar(new LiteralText("REVOLUTION").formatted(Formatting.BOLD), BossBar.Color.RED, BossBar.Style.PROGRESS);
        this.bossBar.setPercent(0.0F);

        for (JsonElement members : this.kingdom.getJson("MEMBERS").getAsJsonArray()) {
            ServerPlayerEntity player = playerManager.getPlayer(UUID.fromString(members.getAsString()));
            if (player != null) this.bossBar.addPlayer(player);
        }

        PlayerDeathCallback.EVENT.register((player, damageSource) -> {
            if (player.getUuid().equals(kingdom.getUuid("KING"))) {
                Entity source = damageSource.getSource();

                if (source instanceof PlayerEntity) {
                    KingdomProcedures.updateKing(kingdomId, source.getUuid());

                    for (JsonElement members : this.kingdom.getJson("MEMBERS").getAsJsonArray()) {
                        NotificationManager.send(UUID.fromString(members.getAsString()), source.getName() + " is the new king of " + kingdom.getId() + "!", NotificationTypes.ACHIEVEMENT);
                    }
                    this.stopEvent();
                }
            }
            return ActionResult.PASS;
        });
    }

    @Override
    public void loop() {
        minPassed++;
        this.bossBar.setPercent((float) minPassed / 15 * 100);
    }

    @Override
    public void finish() {
        for (JsonElement members : this.kingdom.getJson("MEMBERS").getAsJsonArray()) {
            PlayerEntity player = playerManager.getPlayer(UUID.fromString(members.getAsString()));
            if (player != null) player.sendMessage(new LiteralText("The revolution has ended and the king remains in power"), false);
        }
    }
}
