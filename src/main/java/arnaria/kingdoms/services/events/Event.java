package arnaria.kingdoms.services.events;

import arnaria.kingdoms.util.BetterPlayerManager;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class Event {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ServerBossBar bossBar;
    private final int min;
    private final ArrayList<ServerPlayerEntity> members = new ArrayList<>();
    private int secondsPassed = 0;

    public Event(int min, String bossBarText) {
        this.min = min;

        this.bossBar = new ServerBossBar(new LiteralText(bossBarText).formatted(Formatting.BOLD), BossBar.Color.RED, BossBar.Style.PROGRESS);
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

    protected void addPlayers(ArrayList<UUID> uuids) {
        uuids.iterator().forEachRemaining(uuid -> addPlayer(BetterPlayerManager.getPlayer(uuid)));
    }

    public abstract void onDeath(ServerPlayerEntity player);

    protected abstract void finish();

    public ArrayList<ServerPlayerEntity> getParticipants() {
        return this.members;
    }
}
