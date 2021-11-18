package arnaria.kingdoms.services.events;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class EventManager {

    private static final ArrayList<Event> activeEvents = new ArrayList<>();

    public static Event getEvent(ServerPlayerEntity player) {
        for (Event event : activeEvents) {
            if (event.getParticipants().contains(player)) return event;
        }
        return null;
    }

    public static boolean isPlayerInEvent(ServerPlayerEntity player) {
        for (Event event : activeEvents) {
            if (event.getParticipants().contains(player)) return true;
        }
        return false;
    }

    public static void startRevolution(String kingdomId) {
        activeEvents.add(new RevolutionEvent(kingdomId));
    }

    public static void startInvasion(String defendingKingdomId, String attackingKingdomId, BlockPos pos) {

    }

    public static ArrayList<Event> getActiveEvents() {
        return activeEvents;
    }
}
