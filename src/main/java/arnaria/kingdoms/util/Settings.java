package arnaria.kingdoms.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import mrnavastar.sqlib.api.SqlTypes;

import static arnaria.kingdoms.Kingdoms.MODID;

@Config(name = MODID)
public class Settings implements ConfigData {
    @Comment("Allowed Values: \"SQLITE\" | \"MYSQL\"")
    public String DATABASE_TYPE = SqlTypes.SQLITE;
    public String DATABASE_NAME = "Kingdoms";

    public String SQLITE_DIRECTORY = "/path/to/folder";
}
