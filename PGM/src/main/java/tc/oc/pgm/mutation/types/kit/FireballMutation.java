package tc.oc.pgm.mutation.types.kit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import tc.oc.pgm.killreward.KillReward;
import tc.oc.pgm.kits.FreeItemKit;
import tc.oc.pgm.kits.ItemKit;
import tc.oc.pgm.match.Match;
import tc.oc.pgm.mutation.types.KitMutation;

public class FireballMutation extends KitMutation {

	final static String   FIREBALL_NAME = ChatColor.RED + ChatColor.BOLD.toString() + "Fireball";
	final static Material FIREBALL_MATERIAL = Material.FIREBALL;
	
    final static ItemKit FIREBALL = new FreeItemKit(item(FIREBALL_NAME, FIREBALL_MATERIAL, 1));
     
	
	public FireballMutation(Match match) {
		super(match, true, FIREBALL);		
		
		this.rewards.add(new KillReward(FIREBALL));		
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event){
		Player   player = event.getActor();
		Location spawnLoc = player.getLocation();
		ItemStack item = player.getInventory().getItemInMainHand();
		
		if(item != null && item.getType() == FIREBALL_MATERIAL){			
			Fireball fireball = player.launchProjectile(Fireball.class, spawnLoc.getDirection());		
			fireball.setIsIncendiary(true);
			
			if(item.getAmount() > 1){
				item.setAmount(item.getAmount() - 1);
			}else{
				player.getInventory().setItemInMainHand(null);
			}
		}
	}
	
}
