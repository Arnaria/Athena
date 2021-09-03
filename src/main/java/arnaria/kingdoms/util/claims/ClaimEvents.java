package arnaria.kingdoms.util.claims;

import arnaria.kingdoms.interfaces.PlayerEntityInf;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class ClaimEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            return ClaimManager.actionAllowedAt(pos, ((PlayerEntityInf) player).getKingdomId());
        });
    }
}
