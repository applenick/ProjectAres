package tc.oc.pgm.chat;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.entity.Player;

import com.applenick.Lightning.Lightning;
import com.applenick.Lightning.users.ThunderUser;
import com.applenick.Lightning.users.ThunderUsers;

import net.md_5.bungee.api.ChatColor;
import tc.oc.api.bukkit.users.BukkitUserStore;
import tc.oc.api.minecraft.MinecraftService;
import tc.oc.commons.bukkit.chat.FlairRenderer;
import tc.oc.commons.bukkit.chat.NameFlag;
import tc.oc.commons.bukkit.chat.NameType;
import tc.oc.commons.bukkit.nick.Identity;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.match.MatchManager;

/**
 * Add mapmaker flair
 */
@Singleton
public class MatchFlairRenderer extends FlairRenderer {

	//TSPVP MapMakmaker flair
	private static final String MAPMAKER_FLAIR_LEGACY = ChatColor.BLUE + "\u2733";

	private final MatchManager matchManager;

	@Inject MatchFlairRenderer(MinecraftService minecraftService, BukkitUserStore userStore, MatchManager matchManager) {
		super(minecraftService, userStore);
		this.matchManager = matchManager;
	}

	@Override
	public String getLegacyName(Identity identity, NameType type) {
		String name = super.getLegacyName(identity, type);

		if(!type.style.contains(NameFlag.MAPMAKER)) return name;

		// If we ever have multiple simulataneous matches, the mapmaker flair will show
		// in all matches, not just the one for the player's map. We can't avoid this
		// without some way to render names differently in each match (which we could do).
		for(Match match : matchManager.currentMatches()) {   

			if(!match.isUnloaded()) {
				Player player = identity.getPlayer();

				if(match.getMap().getInfo().isAuthor(identity.getPlayerId())){
					name = MAPMAKER_FLAIR_LEGACY + getFlairName(player, name);
					break;
				}

				if(identity.getPlayer() != null){
					name = getFlairName(player, name);
					break;
				}
			}
		}
		return name;
	}
	
	//Thunderstorm PvP - We use our own backend to fetch a ranks & their proper flair
	//Only bug atm is new flairs update after a map cycle or user restarts their session
	private String getFlairName(Player player, String name){
		ThunderUsers users = Lightning.get().getUsers();
		ThunderUser user = users.getThunderUser(player.getUniqueId());
		if(user != null){
			if(user.hasFlair() && !user.isDisguised()){
				return user.getFlair() + name;
			}
		}
		return name;
	}
}
