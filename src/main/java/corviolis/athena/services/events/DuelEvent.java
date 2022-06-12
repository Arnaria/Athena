package corviolis.athena.services.events;

import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.interfaces.ServerPlayerEntityInf;
import corviolis.athena.services.procedures.KingdomProcedures;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class DuelEvent extends Event {

    private final boolean isXpDuel;
    private final List<ServerPlayerEntity> kingdom1;
    private final List<ServerPlayerEntity> kingdom2;
    private final List<ServerPlayerEntity> kingdom1dead = new ArrayList<>();
    private final List<ServerPlayerEntity> kingdom2dead = new ArrayList<>();

    public DuelEvent(List<ServerPlayerEntity> kingdom1, List<ServerPlayerEntity> kingdom2, boolean isXpDuel) {
        super(6, "Duel");
        this.kingdom1 = kingdom1;
        this.kingdom2 = kingdom2;

        kingdom1.forEach(this::addPlayer);
        kingdom2.forEach(this::addPlayer);
        this.isXpDuel = isXpDuel;
    }

    @Override
    public void onDeath(ServerPlayerEntity player, ServerPlayerEntity killer) {
        if (kingdom1.contains(player)) {
            kingdom1.remove(player);
            kingdom1dead.add(player);
            ((ServerPlayerEntityInf) player).trackEntity(kingdom1.get(0));
        } else {
            kingdom2.remove(player);
            kingdom2dead.add(player);
            ((ServerPlayerEntityInf) player).trackEntity(kingdom2.get(0));
        }

        if (kingdom1.size() == 0 || kingdom2.size() == 0) stopEvent();
    }

    @Override
    protected void finish() {
        List<ServerPlayerEntity> winningKingdom;
        List<ServerPlayerEntity> deadKingdom;
        if (kingdom1.size() < kingdom2.size()) {
            kingdom2.addAll(kingdom2dead);
            winningKingdom = kingdom2;
            deadKingdom = kingdom1dead;
        } else {
            kingdom1.addAll(kingdom1dead);
            winningKingdom = kingdom1;
            deadKingdom = kingdom2dead;
        }

        for (ServerPlayerEntity player : winningKingdom) {
            TitleS2CPacket packet = new TitleS2CPacket(Text.literal("Victory!").formatted(Formatting.GREEN).formatted(Formatting.BOLD));
            player.networkHandler.sendPacket(packet);

            ((ServerPlayerEntityInf) player).stopTrackingEntity();
            ((PlayerEntityInf) player).addWin();
            ((PlayerEntityInf) player).addStreak();
            if (isXpDuel) KingdomProcedures.addXp(((PlayerEntityInf) player).getKingdomId(), 15);
        }

        for (ServerPlayerEntity player : deadKingdom) {
            TitleS2CPacket packet = new TitleS2CPacket(Text.literal("Defeat").formatted(Formatting.RED).formatted(Formatting.BOLD));
            player.networkHandler.sendPacket(packet);

            ((ServerPlayerEntityInf) player).stopTrackingEntity();
            ((PlayerEntityInf) player).addLoss();
            ((PlayerEntityInf) player).resetStreak();
        }
    }

    public void cancel(ServerPlayerEntity executor) {
        if ((kingdom1.remove(executor) && kingdom1.size() != 0) || (kingdom2.remove(executor) && kingdom2.size() != 0)) {
            kingdom1dead.remove(executor);
            kingdom2dead.remove(executor);
            removePlayer(executor);
        } else stopEvent();
    }
}
