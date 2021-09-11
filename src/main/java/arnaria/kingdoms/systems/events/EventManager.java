package arnaria.kingdoms.systems.events;

import java.util.ArrayList;

public class EventManager {

    private static final ArrayList<Event> activeEvents = new ArrayList<>();

    public static void startRevolution(String kingdomId) {
        activeEvents.add(new RevolutionEvent(kingdomId));
    }

    public static ArrayList<Event> getActiveEvents() {
        return activeEvents;
    }
}
