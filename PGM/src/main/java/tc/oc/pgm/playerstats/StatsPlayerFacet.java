package tc.oc.pgm.playerstats;

import java.text.DecimalFormat;
import javax.inject.Inject;

import me.anxuiz.settings.SettingManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.applenick.Lightning.Lightning;
import com.applenick.Lightning.users.ThunderUser;

import tc.oc.commons.bukkit.event.targeted.TargetedEventHandler;
import tc.oc.commons.core.chat.Component;
import tc.oc.commons.core.scheduler.Task;
import tc.oc.pgm.events.ListenerScope;
import tc.oc.pgm.events.MatchPlayerDeathEvent;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.match.MatchPlayerFacet;
import tc.oc.pgm.match.MatchScheduler;
import tc.oc.pgm.match.MatchScope;
import tc.oc.pgm.match.inject.ForRunningMatch;

@ListenerScope(MatchScope.RUNNING)
public class StatsPlayerFacet implements MatchPlayerFacet, Listener {

    private static final int DISPLAY_TICKS = 60;
    private static final DecimalFormat FORMAT = new DecimalFormat("0.00");

    private final MatchScheduler scheduler;
    private final StatsUserFacet statsUserFacet;
    private final MatchPlayer player;
    private final SettingManager settings;
    private Task task = null;

    @Inject
    private StatsPlayerFacet(@ForRunningMatch MatchScheduler scheduler, StatsUserFacet statsUserFacet, MatchPlayer player, SettingManager settings) {
        this.scheduler = scheduler;
        this.statsUserFacet = statsUserFacet;
        this.player = player;
        this.settings = settings;
    }

    @TargetedEventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(final MatchPlayerDeathEvent event) {
        if (event.isVictim(this.player) || event.isKiller(this.player)) update();
    }

    private void update() {
        if (!settings.getValue(StatSettings.STATS, Boolean.class)) return;
        if (task != null) {
            task.cancel();
        }
        
        StatSettings.StatTypes statType = settings.getValue(StatSettings.STAT_TYPE, StatSettings.StatTypes.class);
        
        int matchKills  = statsUserFacet.lifeKills();
        int totalKills  = statsUserFacet.matchKills();
        int totalDeaths = statsUserFacet.deaths();
        
        if(statType == StatSettings.StatTypes.GLOBAL){
        	ThunderUser user = Lightning.get().getUsers().getThunderUser(player.getBukkit().getUniqueId());
        	if(user != null){
            	totalKills = user.getKills();
            	totalDeaths = user.getDeaths();
        	}
        }
        
        sendStats(matchKills, totalKills, totalDeaths, (statType == StatSettings.StatTypes.MATCH));
    }

    protected void sendStats(int matchKills, int kills, int deaths, boolean match){
    	task = scheduler.createRepeatingTask(1, 1, new Runnable() {
            int ticks = DISPLAY_TICKS;
            @Override
            public void run() {
                if (--ticks > 0) {
                    player.sendHotbarMessage(getMessage(matchKills, kills, deaths, match));
                } else {
                    delete();
                }
            }
        });
    }
    
    protected TranslatableComponent getMessage(int matchKills, int kills, int deaths, boolean match) {
        TranslatableComponent component = new TranslatableComponent((match ? "stats.hotbar.match" : "stats.hotbar.global"),
                new Component(matchKills, ChatColor.GREEN),
                new Component(kills, ChatColor.GREEN),
                new Component(deaths, ChatColor.RED),
                new Component(FORMAT.format((double) kills / Math.max(deaths, 1)), ChatColor.AQUA));
        component.setBold(true);
        return component;
    }

    protected void delete() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void disable() {
        delete();
    }

}
