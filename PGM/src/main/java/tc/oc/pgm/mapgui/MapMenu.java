package tc.oc.pgm.mapgui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.applenick.Lightning.utils.ThunderUtils;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;

import tc.oc.commons.bukkit.localization.Translations;
import tc.oc.commons.core.chat.ChatUtils;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.plugin.PluginFacet;
import tc.oc.pgm.PGM;
import tc.oc.pgm.gui.PageGui;
import tc.oc.pgm.map.Contributor;
import tc.oc.pgm.map.MapLibrary;
import tc.oc.pgm.map.PGMMap;
import tc.oc.pgm.map.PGMMap.DisplayOrder;
import tc.oc.pgm.match.MatchPlayer;
import tc.oc.pgm.modules.InfoModule;

@Singleton 
public class MapMenu extends PageGui implements PluginFacet{

	public final static String MENU_TITLE = "command.maps.title"; //Inv title key

	public final static String NEXT_PAGE_TEXT = "Next Page";
	public final static String PREV_PAGE_TEXT = "Previous Page";

	public final static int MENU_SIZE = 45; //Inventory size

	public final static int MAPS_PER_PAGE = 18; //# of maps on each page

	public final static Material MAP_ITEM  = Material.MAP;
	public final static Material PAGE_ITEM = Material.ARROW;

	@Inject MapLibrary mapLibrary;

	public MapMenu() {
		super(MENU_TITLE, MENU_SIZE);
	}

	public void changePage(MatchPlayer player, boolean next, boolean force){
		int currentPage = getPage(player);

		if(next){
			if(force){
				currentPage = getTotalPages();
			}else{
				currentPage++;
			}
		}else{
			if(force){
				currentPage = 1;
			}else{
				//Failsafe as to not go before the first page, just in case
				if(currentPage > 1){
					currentPage--;
				}
			}
		}

		setPage(player, currentPage);
		this.refreshWindow(player, currentPage);
	}

	@Override
	public ItemStack[] createWindowContents(MatchPlayer player, int page) {
		List<ItemStack> items = new ArrayList<>();//Holds all items in the inv

		int maps = 0;//Used to see how many slots have been used so far

		//get & create the mapicons per that page
		for(MapIcon map : getMaps(page)){
			items.add(map.getIcon(player));
			items.add(null);//One slot spacing between each map

			maps += 2;
		}

		//If the # of maps on that page are less than the MAX, then add null selections until we reach the amount
		if(maps != MAPS_PER_PAGE){
			while(maps < MAPS_PER_PAGE){
				items.add(null);
				maps++;
			}
		}

		//Every page after the first page gets a previous page button
		if(page > 1){
			items.add(getPageItem(false , page - 1));
			maps++;
		}

		//Add null items until the last slot, which is where the next page item goes
		while(maps < MENU_SIZE - 1){
			items.add(null);
			maps++;
		}

		if((page + 1) <= getTotalPages()){
			items.add(getPageItem(true, page + 1));
		}

		return items.toArray(new ItemStack[items.size()]);
	}


	public ItemStack getPageItem(boolean next, int page){
		ItemStack stack = new ItemStack(PAGE_ITEM, page);
		ItemMeta  meta  = stack.getItemMeta();
		String pageText = (next ? NEXT_PAGE_TEXT : PREV_PAGE_TEXT);
		
		meta.setDisplayName(ChatColor.GRAY + pageText);
		meta.addItemFlags(ItemFlag.values());
		
		stack.setItemMeta(meta);
		return stack;
	}

	public boolean isPageButton(String text){
		return text.equalsIgnoreCase(NEXT_PAGE_TEXT) || text.equalsIgnoreCase(PREV_PAGE_TEXT);
	}

	public int getTotalPages(){
		return PGM.getMatchManager().getMaps().size() / MAPS_PER_PAGE;		
	}

	public List<MapIcon> getMaps(int page){
		List<MapIcon> mapIcons = Lists.newArrayList();
		final Set<PGMMap> maps = ImmutableSortedSet.copyOf(new PGMMap.DisplayOrder(), PGM.getMatchManager().getMaps());
		int totalPages = maps.size() / MAPS_PER_PAGE + 1;

		if(totalPages % MAPS_PER_PAGE == 0){
			totalPages--;
		}


		List<PGMMap> orderMaps = new ArrayList<PGMMap>(maps);

		for(int index = MAPS_PER_PAGE * (page - 1); index < MAPS_PER_PAGE * page; index++){
			PGMMap map = orderMaps.get(index);

			mapIcons.add(new MapIcon(map, index));
		}

		//TODO: filter maps
		return mapIcons;
	}

	public static class MapIcon{

		private int slot;
		private PGMMap map;

		public MapIcon(PGMMap map, int slot){
			this.map = map;
			this.slot = slot;
		}

		public int getSlot(){
			return slot;
		}

		public ItemStack getIcon(MatchPlayer player){
			ItemStack stack = new ItemStack(MAP_ITEM);
			ItemMeta  meta = stack.getItemMeta();
			List<String> lore = Lists.newArrayList();

			//Map Name
			meta.setDisplayName(map.getInfo().getColoredName());


			String author = "";
			List<Contributor> authors = map.getInfo().getNamedAuthors();
			if(!authors.isEmpty()) {
				author = Translations.get().t(
						ChatColor.GRAY.toString(),
						"misc.authorship",
						player.getBukkit(),
						author,
						Translations.get().legacyList(
								player.getBukkit(),
								ChatColor.GRAY.toString(),
								ChatColor.AQUA.toString(),
								authors
								)
						);
			}

			ChatUtils.wordWrap(author, 136).forEach(line -> lore.add(line));

			//Map Info
			final InfoModule infoModule = map.getContext().needModule(InfoModule.class);
			lore.add(ChatColor.GOLD + "Gamemode: " + infoModule.getGamemodeTag());
			lore.add(ChatColor.DARK_AQUA + "Max Players: " + ChatColor.AQUA + map.getDocument().max_players());

			lore.add("");
			lore.add(ChatColor.GREEN + "Click to for more info");
						
			if(player.getBukkit().hasPermission("pgm.next.set")){
				lore.add("");
				lore.add(ThunderUtils.DIV + ChatColor.YELLOW + "Right Click to Set Next" + ThunderUtils.RDIV);
			}

			meta.setLore(lore);
			meta.addItemFlags(ItemFlag.values());

			stack.setItemMeta(meta);

			return stack;
		}

		public PGMMap getMap(){
			return map;
		}
	}
}