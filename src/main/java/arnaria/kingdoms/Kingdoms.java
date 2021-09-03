package arnaria.kingdoms;

import arnaria.kingdoms.util.Settings;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import mrnavastar.sqlib.api.SqlTypes;
import mrnavastar.sqlib.api.Table;
import mrnavastar.sqlib.util.Database;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class Kingdoms implements ModInitializer {

    public static final String MODID = "Kingdoms";
    public static Table kingdomsData;
    public static Settings settings;

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing...");

        boolean validConfig = false;
        AutoConfig.register(Settings.class, JanksonConfigSerializer::new);
        settings = AutoConfig.getConfigHolder(Settings.class).getConfig();

        if (settings.DATABASE_TYPE.equals(SqlTypes.SQLITE)) {
            validConfig = !settings.SQLITE_DIRECTORY.equals("/path/to/folder");
        }

        if (settings.DATABASE_TYPE.equals(SqlTypes.MYSQL)) {
            validConfig = (!settings.MYSQL_USERNAME.equals("username") && !settings.MYSQL_PASSWORD.equals("password"));
        }

        if (validConfig) {
            Database.TYPE = settings.DATABASE_TYPE;
            Database.DATABASE_NAME = settings.DATABASE_NAME;

            Database.SQLITE_DIRECTORY = settings.SQLITE_DIRECTORY;

            Database.MYSQL_ADDRESS = settings.MYSQL_ADDRESS;
            Database.MYSQL_PORT = settings.MYSQL_PORT;
            Database.MYSQL_USERNAME = settings.MYSQL_USERNAME;
            Database.MYSQL_PASSWORD = settings.MYSQL_PASSWORD;

            Database.init();

            kingdomsData = new Table("KingdomsData");
        }
    }

    public static void log(Level level, String message){
        LogManager.getLogger().log(level, "[" + MODID + "] " + message);
    }
}
