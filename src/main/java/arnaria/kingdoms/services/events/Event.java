package arnaria.kingdoms.services.events;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class Event {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public Event(int min) {
        Runnable loop = this::loop;
        Runnable finish = this::stopEvent;

        scheduler.scheduleAtFixedRate(loop, 1, 1, TimeUnit.SECONDS);
        scheduler.schedule(finish, min, TimeUnit.MINUTES);
    }

    protected void stopEvent() {
        scheduler.shutdownNow();
        finish();
    }

    public abstract void loop();

    public abstract void finish();
}
