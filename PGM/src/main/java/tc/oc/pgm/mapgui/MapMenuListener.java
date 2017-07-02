package tc.oc.pgm.mapgui;

import java.util.Optional;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.applenick.Lightning.utils.ThunderUtils;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.md_5.bungee.api.ChatColor;
import tc.oc.pgm.PGM;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.match.MatchManager;

public class MapMenuListener implements Listener {

	private MapMenu menu;
	private MatchManager manager;
	
	@Inject MapLibrary mapLibrary;


	public MapMenuListener(){
		menu = PGM.get().getMapMenu();
		manager = PGM.getMatchManager();
	}

	@EventHandler
	public void onMenuClick(InventoryClickEvent event){	
		if(manager == null){ return;}
		
		manager.getCurrentMatch().player(event.getActor()).ifPresent(player -> {

			if(menu.isViewing(player)){

				if(event.getCurrentItem() != null){
					ItemStack clickedItem = event.getCurrentItem();
					ItemMeta  itemMeta    = clickedItem.getItemMeta();
					String    itemName    = itemMeta.getDisplayName();
					itemName = ChatColor.stripColor(Preconditions.checkNotNull(itemName));

					if(clickedItem.getType() == MapMenu.MAP_ITEM){
						
						if(event.getActor().hasPermission("pgm.next.set") && event.isRightClick()){
							event.getActor().performCommand("sn " + itemName);
						}else{
							event.getActor().performCommand("map " + itemName);
						}
						
						menu.close(player);
					}

					if(clickedItem.getType() == MapMenu.PAGE_ITEM){
						boolean force = event.isShiftClick();
						
						if(menu.isPageButton(itemName)){
							boolean next = itemName.equalsIgnoreCase(MapMenu.NEXT_PAGE_TEXT);
							menu.changePage(player, next, force);
						}
					}

				}
				event.setCancelled(true);
			}

		});
	}


	@EventHandler
	public void onMenuClose(InventoryCloseEvent event){
		manager.getCurrentMatch().player(event.getActor()).ifPresent(player -> menu.removeViewer(player));
	}

}
