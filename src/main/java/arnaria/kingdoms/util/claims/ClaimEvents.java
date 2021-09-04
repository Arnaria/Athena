package arnaria.kingdoms.util.claims;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class ClaimEvents {

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> ClaimManager.actionAllowedAt(pos, player));
    }
}
