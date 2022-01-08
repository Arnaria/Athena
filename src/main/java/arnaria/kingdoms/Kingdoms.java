package arnaria.kingdoms;

import arnaria.kingdoms.commands.*;
import arnaria.kingdoms.services.api.RestAPI;
import arnaria.kingdoms.services.claims.NewClaimManager;
import arnaria.kingdoms.services.events.ChallengeManager;
import arnaria.kingdoms.util.Settings;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import com.mojang.brigadier.CommandDispatcher;
import de.bluecolored.bluemap.api.BlueMapAPI;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import mrnavastar.sqlib.api.databases.Database;
import mrnavastar.sqlib.api.databases.SQLiteDatabase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.UserCache;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;

public class Kingdoms implements ModInitializer {

    public static final String MODID = "Kingdoms";
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

        boolean validConfig = false;
        AutoConfig.register(Settings.class, JanksonConfigSerializer::new);
        settings = AutoConfig.getConfigHolder(Settings.class).getConfig();

        if (settings.DATABASE_TYPE.equals("SQLITE")) {
            validConfig = !settings.SQLITE_DIRECTORY.equals("/path/to/folder");
        }

        if (settings.clearDatabaseOnBoot && validConfig) {
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
                //ClaimManager.init();
                NewClaimManager.init();
                RestAPI.init();

                //Command Registration
                CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
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
                    arnaria.kingdoms.util.BlueMapAPI.init(api.getMarkerAPI());
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
