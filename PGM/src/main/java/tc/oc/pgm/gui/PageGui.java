package tc.oc.pgm.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.pgm.PGMTranslations;
import tc.oc.pgm.match.MatchPlayer;

public abstract class PageGui {


	public Set<MatchPlayer> viewing = Sets.newHashSet();
	public HashMap<MatchPlayer,Integer> viewerPages = Maps.newHashMap();

	public String title;
	private int size;

	public static final int WIDTH = 9; // Inventory width in slots - for readability

	public PageGui(String title, int size){
		this.title = title;
		this.size = size;
	}

	public int getPage(MatchPlayer player){
		return (viewerPages.get(player) != null ? viewerPages.get(player) : 1);
	}

	public void setPage(MatchPlayer player, int page){
		this.viewerPages.put(player, page);
	}
	
	//The title of the inventory with added page #
	public String getTranslatedTitle(MatchPlayer player){
		return ChatColor.GREEN + ChatColor.BOLD.toString() + PGMTranslations.t(title, player) + ChatColor.GOLD + " \u00BB " + ChatColor.DARK_AQUA + "Page " + ChatColor.AQUA + getPage(player);
	}

	public void display(MatchPlayer player){
		this.showWindow(player, getPage(player));
		addViewer(player);
	}

	public void close(MatchPlayer player){
		scheduleClose(player);
		removeViewer(player);
	}

	private void scheduleClose(final MatchPlayer player) {
		player.nextTick(() -> {
			player.getBukkit().getOpenInventory().getTopInventory().clear();
			player.getBukkit().closeInventory();
		});
	}
	
	
	public boolean isViewing(MatchPlayer player){
		return this.viewing.contains(player);
	}
	
	public void removeViewer(MatchPlayer player){
		this.viewing.remove(player);
	}
	
	public void addViewer(MatchPlayer player){
		this.viewing.add(player);
	}


	/**
	 * Open the window for the given player, or refresh its contents
	 * if they already have it open, and return the current contents.
	 *
	 * If the window is currently open but too small to hold the current
	 * contents, it will be closed and reopened.
	 *
	 * If the player is not currently allowed to have the window open,
	 * close any window they have open and return null.
	 */
	private @Nullable Inventory showWindow(MatchPlayer player, int page) {    	
		ItemStack[] contents = createWindowContents(player, page);
		Inventory inv = getOpenWindow(player);
		if(inv != null && inv.getSize() < contents.length) {
			inv = null;
			closeWindow(player);
		}
		if(inv == null) {
			inv = openWindow(player, contents);
		} else {
			inv.setContents(contents);
		}
		return inv;
	}

	/**
	 * If the given player currently has the window open, refresh its contents
	 * and return the updated inventory. The window will be closed and reopened
	 * every time as to update the inventory title.
	 *
	 * If the window is open but should be closed, close it and return null.
	 *
	 * If the player does not have the window open, return null.
	 */
	protected @Nullable Inventory refreshWindow(MatchPlayer player, int page) {
		Inventory inv = getOpenWindow(player);
		if(inv != null) {
			ItemStack[] contents = createWindowContents(player, page);
			closeWindow(player);
			inv = openWindow(player, contents);
		}
		return inv;
	}



	/**
	 * Return the inventory of the given player's currently open window,
	 * or null if the player does not have the window open.
	 */
	private @Nullable Inventory getOpenWindow(MatchPlayer player) {
		if(viewing.contains(player)) {
			return player.getBukkit().getOpenInventory().getTopInventory();
		}
		return null;
	}

	/**
	 * Close any window that is currently open for the given player
	 */
	private void closeWindow(MatchPlayer player) {
		if(viewing.contains(player)) {
			player.getBukkit().closeInventory();
		}
	}

	/**
	 * Open a new window for the given player displaying the given contents
	 */
	private Inventory openWindow(MatchPlayer player, ItemStack[] contents) {
		closeWindow(player);
		Inventory inv = player.getMatch().getServer().createInventory(player.getBukkit(), size, StringUtils.truncate(getTranslatedTitle(player), 32));

		inv.setContents(contents);
		player.getBukkit().openInventory(inv);
		viewing.add(player);
		return inv;
	}


	/**
	 * Defines how the GUI will display the layout
	 */
	public abstract ItemStack[] createWindowContents(final MatchPlayer player, int page);

}
