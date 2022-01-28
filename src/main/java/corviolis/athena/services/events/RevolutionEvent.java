package corviolis.athena.services.events;

import corviolis.athena.Athena;
import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.services.procedures.KingdomProcedures;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import corviolis.athena.util.BetterPlayerManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class RevolutionEvent extends Event {

    private final String kingdomId;
    private final Team kingdomTeam;
    private ServerPlayerEntity newKing;

    public RevolutionEvent(String kingdomId) {
        super(Athena.settings.REVOLUTION_DURATION, kingdomId + " | Revolution");
        this.kingdomId = kingdomId;
        kingdomTeam = Athena.scoreboard.getTeam(kingdomId);
        if (kingdomTeam != null) kingdomTeam.setFriendlyFireAllowed(true);

        for (UUID uuid : KingdomsData.getMembers(kingdomId)) {
            ServerPlayerEntity player = BetterPlayerManager.getPlayer(uuid);
            if (player != null) addPlayer(player);
        }
    }

    public String getKingdomId() {
        return kingdomId;
    }

    @Override
    public void onDeath(ServerPlayerEntity player, ServerPlayerEntity killer) {
        if (((PlayerEntityInf) player).isKing()) {
            newKing = killer;
            stopEvent();
        }
    }

    @Override
    protected void finish() {
        KingdomProcedures.endRevolution(kingdomId);
        if (kingdomTeam != null) kingdomTeam.setFriendlyFireAllowed(false);
        if (newKing != null) {
            for (ServerPlayerEntity participant : this.getParticipants()) {
                if (!((PlayerEntityInf) participant).isKing()) NotificationManager.send(participant.getUuid(), "The Revolution was successful! " + newKing.getName() + " is now king!", NotificationTypes.ACHIEVEMENT);
                else NotificationManager.send(participant.getUuid(), "The Revolution was successful. You are no longer king", NotificationTypes.WARN);
            }
            KingdomProcedures.updateKing(kingdomId, newKing.getUuid());
        } else {
            for (ServerPlayerEntity participant : this.getParticipants()) {
                if (!((PlayerEntityInf) participant).isKing()) NotificationManager.send(participant.getUuid(), "The Revolution was unsuccessful", NotificationTypes.WARN);
                else NotificationManager.send(participant.getUuid(), "The Revolution was unsuccessful! You are still king!", NotificationTypes.WARN);
            }
        }
    }
}