package arnaria.kingdoms;

import arnaria.kingdoms.commands.*;
import arnaria.kingdoms.services.api.RestAPI;
import arnaria.kingdoms.util.Settings;
import arnaria.kingdoms.services.claims.ClaimManager;
import arnaria.kingdoms.services.procedures.KingdomProcedures;
import com.mojang.brigadier.CommandDispatcher;
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

public class Kingdoms implements ModInitializer {

    public static final String MODID = "Kingdoms";
    public static PlayerManager playerManager;
    public static ServerWorld overworld;
    public static UserCache userCache;
    public static Scoreboard scoreboard;
    public static Database database;
    public static Settings settings;

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing...");

        boolean validConfig = false;
        AutoConfig.register(Settings.class, JanksonConfigSerializer::new);
        settings = AutoConfig.getConfigHolder(Settings.class).getConfig();

        if (settings.DATABASE_TYPE.equals("SQLITE")) {
            validConfig = !settings.SQLITE_DIRECTORY.equals("/path/to/folder");
        }

        if (settings.DATABASE_TYPE.equals("MYSQL")) {
            validConfig = (!settings.MYSQL_USERNAME.equals("username") && !settings.MYSQL_PASSWORD.equals("password"));
        }

        if (validConfig) {
            database = new SQLiteDatabase(settings.DATABASE_NAME, settings.SQLITE_DIRECTORY);

            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                playerManager = server.getPlayerManager();
                overworld = server.getOverworld();
                scoreboard = server.getScoreboard();

                ClaimManager.init();
                RestAPI.init();
            });

            ServerLifecycleEvents.SERVER_STARTING.register(server -> {
                userCache = server.getUserCache();

                //Command Registration
                CommandDispatcher<ServerCommandSource> dispatcher = server.getCommandManager().getDispatcher();
                LinkCommand.register(dispatcher);
                ClaimBannerCommand.register(dispatcher);
                GetKingCommand.register(dispatcher);
                ManageSelfCommand.register(dispatcher);
                RevolutionCommand.register(dispatcher);
                PopulationManagerCommands.register(dispatcher);
                ManageKingdomCommand.register(dispatcher);
            });

            ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> KingdomProcedures.setupPlayer(handler.getPlayer()));

            log(Level.INFO, "Setup Finished");
        }
    }

    public static void log(Level level, String message) {
        LogManager.getLogger().log(level, "[" + MODID + "] " + message);
    }
}
