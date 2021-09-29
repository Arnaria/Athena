package arnaria.kingdoms.services.events;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class Event {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ServerBossBar bossBar;
    private final int min;
    private final String name;
    private final ArrayList<ServerPlayerEntity> members = new ArrayList<>();
    private int secondsPassed = 0;

    public Event(int min, String name) {
        this.min = min;
        this.name = name;

        this.bossBar = new ServerBossBar(new LiteralText(name).formatted(Formatting.BOLD), BossBar.Color.RED, BossBar.Style.PROGRESS);
        this.bossBar.setPercent(1.0F);

        Runnable stopEvent = this::stopEvent;
        Runnable updateBossBar = this::updateBossBar;

        scheduler.scheduleAtFixedRate(updateBossBar, 1, 1, TimeUnit.SECONDS);
        scheduler.schedule(stopEvent, min, TimeUnit.MINUTES);
    }

    private void updateBossBar() {
        this.secondsPassed++;
        float percent = (float) secondsPassed / (this.min * 60);
        this.bossBar.setPercent(1 - percent);
    }

    protected void stopEvent() {
        scheduler.shutdownNow();
        this.bossBar.clearPlayers();
        finish();
    }

    protected void addPlayer(ServerPlayerEntity player) {
        this.bossBar.addPlayer(player);
        this.members.add(player);
    }

    protected abstract void finish();

    public String getName() {
        return this.name;
    }

    public ArrayList<ServerPlayerEntity> getMembers() {
        return this.members;
    }
}
