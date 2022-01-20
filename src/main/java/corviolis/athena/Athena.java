package corviolis.athena;

import corviolis.athena.commands.*;
import corviolis.athena.services.api.RestAPI;
import corviolis.athena.services.claims.ClaimManager;
import corviolis.athena.services.events.ChallengeManager;
import corviolis.athena.services.procedures.KingdomProcedures;
import corviolis.athena.util.Settings;
import de.bluecolored.bluemap.api.BlueMapAPI;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import mrnavastar.sqlib.api.databases.Database;
import mrnavastar.sqlib.api.databases.SQLiteDatabase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;

public class Athena implements ModInitializer {

    public static final String MODID = "Athena";
    public static PlayerManager playerManager;
    public static ServerWorld overworld;
    public static UserCache userCache;
    public static Scoreboard scoreboard;
    public static Database database;
    public static Settings settings;
    public static BlueMapAPI blueMapAPI;

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing...");

        AutoConfig.register(Settings.class, JanksonConfigSerializer::new);
        settings = AutoConfig.getConfigHolder(Settings.class).getConfig();
        boolean validConfig = !settings.SQLITE_DIRECTORY.equals("/path/to/folder");

        //Can be removed later if we want
        if (settings.CLEAR_DATABASE_ON_BOOT && validConfig) {
            File database = new File(settings.SQLITE_DIRECTORY + "/" + settings.DATABASE_NAME + ".db");
            database.delete();
        }

        if (validConfig) {
            database = new SQLiteDatabase(settings.DATABASE_NAME, settings.SQLITE_DIRECTORY);

            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                playerManager = server.getPlayerManager();
                userCache = server.getUserCache();
                overworld = server.getOverworld();
                scoreboard = server.getScoreboard();

                KingdomProcedures.init();
                ChallengeManager.init();
                RestAPI.init();
            });

            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
                LinkCommand.register(dispatcher);
                ClaimBannerCommand.register(dispatcher);
                GetKingCommand.register(dispatcher);
                ManageSelfCommand.register(dispatcher);
                RevolutionCommand.register(dispatcher);
                PopulationManagerCommands.register(dispatcher);
                ManageKingdomCommand.register(dispatcher);
                ClaimManagerCommands.register(dispatcher);
            });

            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> KingdomProcedures.setupPlayer(handler.getPlayer()));

            BlueMapAPI.onEnable(api -> {
                try {
                    blueMapAPI = api;
                    corviolis.athena.services.api.BlueMapAPI.init(api.getMarkerAPI());

                    //Must be setup after bluemap as it depends on it
                    ClaimManager.init();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            log(Level.INFO, "Setup Finished");
        }
    }

    public static void log(Level level, String message) {
        LogManager.getLogger().log(level, "[" + MODID + "] " + message);
    }
}
