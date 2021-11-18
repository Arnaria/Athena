package arnaria.kingdoms.services.events;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import arnaria.kingdoms.services.data.KingdomsData;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import arnaria.notifacaitonlib.NotificationManager;
import arnaria.notifacaitonlib.NotificationTypes;
import net.minecraft.server.network.ServerPlayerEntity;

public class RevolutionEvent extends Event {

    private final String kingdomId;
    private ServerPlayerEntity newKing;

    public RevolutionEvent(String kingdomId) {
        super(15, "Revolution");
        this.kingdomId = kingdomId;
        this.addPlayers(KingdomsData.getMembers(kingdomId));
    }

    @Override
    public void onDeath(ServerPlayerEntity player, ServerPlayerEntity killer) {
        if (((PlayerEntityInf) player).isKing()) {
            newKing = killer;
            this.stopEvent();
        }
    }

    @Override
    protected void finish() {
        if (newKing != null) {
            KingdomProcedures.updateKing(kingdomId, newKing.getUuid());
            for (ServerPlayerEntity participant : this.getParticipants()) {
                if (!((PlayerEntityInf) participant).isKing()) {
                    NotificationManager.send(participant.getUuid(), "The Revolution was successful! " + newKing.getName() + " is now king!", NotificationTypes.ACHIEVEMENT);
                } else {
                    NotificationManager.send(participant.getUuid(), "The Revolution was successful. You are no longer king", NotificationTypes.WARN);
                }
            }
        } else {
            for (ServerPlayerEntity participant : this.getParticipants()) {
                if (!((PlayerEntityInf) participant).isKing()) {
                    NotificationManager.send(participant.getUuid(), "The Revolution was unsuccessful", NotificationTypes.WARN);
                } else {
                    NotificationManager.send(participant.getUuid(), "The Revolution was unsuccessful! You are still king!", NotificationTypes.WARN);
                }
            }
        }
    }
}