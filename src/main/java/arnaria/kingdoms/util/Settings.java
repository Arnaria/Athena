package arnaria.kingdoms.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.UUID;

import static arnaria.kingdoms.Kingdoms.MODID;

@Config(name = MODID)
public class Settings implements ConfigData {
    @Comment("Admins")
    public String[] admins = {};

    @Comment("Allowed Values: \"SQLITE\" | \"MYSQL\"")
    public String DATABASE_TYPE = "SQLITE";
    public String DATABASE_NAME = "Kingdoms";

    public String SQLITE_DIRECTORY = "/path/to/folder";

    public String MYSQL_ADDRESS = "127.0.0.1";
    public String MYSQL_PORT = "3306";
    @Comment("The mod will not start if you use these as your actual credentials - please keep your data secure")
    public String MYSQL_USERNAME = "username";
    public String MYSQL_PASSWORD = "password";

    @Comment("Game Settings:")
    public int REVOLUTION_DURATION = 15;
    public int INVASION_DURATION = 15;
}
