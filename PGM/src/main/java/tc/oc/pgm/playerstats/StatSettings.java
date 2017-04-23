package tc.oc.pgm.playerstats;

import me.anxuiz.settings.Setting;
import me.anxuiz.settings.SettingBuilder;
import me.anxuiz.settings.types.BooleanType;
import me.anxuiz.settings.types.EnumType;
import me.anxuiz.settings.types.Name;

public class StatSettings {

    public static final Setting STATS = new SettingBuilder()
            .name("Stats")
            .summary("Show kill and death stats on pvp encounters")
            .type(new BooleanType())
            .defaultValue(true).get();

    
    public static final Setting STAT_TYPE = new SettingBuilder()
            .name("StatType")
            .summary("Display either match stats or global")
            .type(new EnumType<StatTypes>("Stat Types", StatTypes.class))
            .defaultValue(StatTypes.MATCH).get();
    
    public static enum StatTypes{
		@Name("match")
		MATCH,
		
		@Name("global")
		GLOBAL
	}
    
}
