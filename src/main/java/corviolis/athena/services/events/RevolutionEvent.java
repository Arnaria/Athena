package corviolis.athena.services.events;

import corviolis.athena.Athena;
import corviolis.athena.interfaces.PlayerEntityInf;
import corviolis.athena.services.data.KingdomsData;
import corviolis.athena.services.procedures.KingdomProcedures;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import net.minecraft.server.network.ServerPlayerEntity;

public class RevolutionEvent extends Event {

    private final String kingdomId;
    private ServerPlayerEntity newKing;

    public RevolutionEvent(String kingdomId) {
        super(Athena.settings.REVOLUTION_DURATION, kingdomId + " | Revolution");
        this.kingdomId = kingdomId;
        addPlayers(KingdomsData.getMembers(kingdomId));
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