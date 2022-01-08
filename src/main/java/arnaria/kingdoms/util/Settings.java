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

    @Comment("Game Settings")
    public int REVOLUTION_DURATION = 15;
    public int INVASION_DURATION = 15;

    @Comment("Dev Settings")
    public boolean clearDatabaseOnBoot = false;
}
