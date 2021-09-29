package arnaria.kingdoms.services.events;

import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class EventManager {

    private static final ArrayList<Event> activeEvents = new ArrayList<>();

    public static void startRevolution(String kingdomId) {
        activeEvents.add(new RevolutionEvent(kingdomId));
    }

    public static void startInvasion(String defendingKingdomId, String attackingKingdomId, BlockPos pos) {
        activeEvents.add(new InvasionEvent(defendingKingdomId, attackingKingdomId, pos));
    }

    public static ArrayList<Event> getActiveEvents() {
        return activeEvents;
    }
}
