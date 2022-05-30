package corviolis.athena.services.events;

import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.interfaces.ServerPlayerEntityInf;
import corviolis.athena.services.procedures.KingdomProcedures;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.List;

public class DuelEvent extends Event {

    private final boolean isXpDuel;
    private final List<ServerPlayerEntity> kingdom1;
    private final List<ServerPlayerEntity> kingdom2;

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
        this.stopEvent();

        if (kingdom1.contains(player) && kingdom1.size() > 2) {
            kingdom1.remove(player);
            ((ServerPlayerEntityInf) player).trackEntity(kingdom1.get(0));
        }
        else if (kingdom2.size() > 2) {
            kingdom2.remove(player);
            ((ServerPlayerEntityInf) player).trackEntity(kingdom2.get(0));
        }
        else {
            BlockPos spawn = player.getSpawnPointPosition();
            if (spawn != null) player.requestTeleport(spawn.getX(), spawn.getY(), spawn.getZ());
        }

        String loserMessage = "You lost the duel!";
        if (((PlayerEntityInf) player).getStreak() > 0) loserMessage += " Streak of " + ((PlayerEntityInf) player).getStreak() + " lost!";
        player.sendMessage(new LiteralText(loserMessage), false);

        ((PlayerEntityInf) player).addLoss();
        ((PlayerEntityInf) player).resetStreak();
        ((PlayerEntityInf) killer).addWin();
        ((PlayerEntityInf) killer).addStreak();

        //TODO: make xp on a scale based on streak (capped)
        String winnerMessage = "You won the duel!";
        if (isXpDuel) winnerMessage += " Gained 15 xp for your kingdom!";
        killer.sendMessage(new LiteralText(winnerMessage), false);
        KingdomProcedures.addXp(((PlayerEntityInf) killer).getKingdomId(), 15);
    }

    @Override
    protected void finish() {

    }

    public void cancel() {

    }
}
