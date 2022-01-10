package corviolis.athena.util;

import corviolis.athena.Athena;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = Athena.MODID)
public class Settings implements ConfigData {
    public String[] ADMINS = {};
    public int API_PORT = 6969;

    public String DATABASE_NAME = "Athena";
    public String SQLITE_DIRECTORY = "/path/to/folder";

    @Comment("Game Settings")
    public int REVOLUTION_DURATION = 15;
    public int INVASION_DURATION = 15;

    @Comment("Dev Settings")
    public boolean CLEAR_DATABASE_ON_BOOT = false;
}
