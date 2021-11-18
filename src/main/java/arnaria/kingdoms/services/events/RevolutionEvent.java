package arnaria.kingdoms.services.events;

import arnaria.kingdoms.services.data.KingdomsData;
import net.minecraft.server.network.ServerPlayerEntity;

public class RevolutionEvent extends Event {

    public RevolutionEvent(String kingdomId) {
        super(15, "Revolution");
        addPlayers(KingdomsData.getMembers(kingdomId));


    }

    @Override
    public void onDeath(ServerPlayerEntity player) {

    }

    @Override
    protected void finish() {

    }
}
