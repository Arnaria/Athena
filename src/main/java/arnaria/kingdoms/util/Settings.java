package arnaria.kingdoms.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import static arnaria.kingdoms.Kingdoms.MODID;

@Config(name = MODID)
public class Settings implements ConfigData {
    public String[] ADMINS = {};
    public int API_PORT = 6969;

    @Comment("Allowed Values: \"SQLITE\" | \"MYSQL\"")
    public String DATABASE_TYPE = "SQLITE";
    public String DATABASE_NAME = "Kingdoms";

    public String SQLITE_DIRECTORY = "/path/to/folder";

    @Comment("Game Settings")
    public int REVOLUTION_DURATION = 15;
    public int INVASION_DURATION = 15;

    @Comment("Dev Settings")
    public boolean CLEAR_DATABASE_ON_BOOT = false;
}
