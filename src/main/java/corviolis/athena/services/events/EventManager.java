package corviolis.athena.services.events;

import net.minecraft.server.network.ServerPlayerEntity;

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

    public static boolean isKingdomInRevolt(String kingdomId) {
        for (Event event : activeEvents) {
            if (event instanceof RevolutionEvent revolt && kingdomId.equals(revolt.getKingdomId())) return true;
        }
        return false;
    }

    public static void startRevolution(String kingdomId) {
        activeEvents.add(new RevolutionEvent(kingdomId));
    }

    public static void startDuel(ServerPlayerEntity player1, ServerPlayerEntity player2, boolean isXpDuel) {
        activeEvents.add(new DuelEvent(player1, player2, isXpDuel, kingdom11));
    }

    public static ArrayList<Event> getActiveEvents() {
        return activeEvents;
    }
}
