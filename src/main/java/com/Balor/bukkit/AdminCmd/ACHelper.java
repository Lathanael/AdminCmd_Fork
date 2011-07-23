package com.Balor.bukkit.AdminCmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

import be.Balor.Manager.PermissionManager;
import com.Balor.files.utils.FilesManager;
import com.Balor.files.utils.MaterialContainer;
import com.Balor.files.utils.Utils;
import com.google.common.collect.MapMaker;

/**
 * Handle commands
 * 
 * @authors Plague, Balor
 */
public class ACHelper {

	private CommandSender sender;
	private HashMap<Material, String[]> materialsColors;
	private List<Integer> listOfPossibleRepair;
	private FilesManager fManager;
	private List<Integer> blacklist;
	private AdminCmd pluginInstance;
	ConcurrentMap<String, ConcurrentMap<String, Object>> usersWithPowers = new MapMaker().makeMap();
	private ConcurrentMap<String, MaterialContainer> alias = new MapMaker().makeMap();
	private ConcurrentMap<String, Location> spawnLocations = new MapMaker().softValues()
			.expiration(20, TimeUnit.MINUTES).makeMap();
	private static ACHelper instance = null;

	private ACHelper() {
		materialsColors = new HashMap<Material, String[]>();
		materialsColors.put(Material.WOOL, new String[] { "White", "Orange", "Magenta",
				"LightBlue", "Yellow", "LimeGreen", "Pink", "Gray", "LightGray", "Cyan", "Purple",
				"Blue", "Brown", "Green", "Red", "Black" });
		materialsColors.put(Material.INK_SACK, new String[] { "Black", "Red", "Green", "Brown",
				"Blue", "Purple", "Cyan", "LightGray", "Gray", "Pink", "LimeGreen", "Yellow",
				"LightBlue", "Magenta", "Orange", "White" });
		materialsColors.put(Material.LOG, new String[] { "Oak", "Pine", "Birch" });
		materialsColors.put(Material.STEP, new String[] { "Stone", "Sandstone", "Wooden",
				"Cobblestone" });
		materialsColors.put(Material.DOUBLE_STEP, materialsColors.get(Material.STEP));
		listOfPossibleRepair = new LinkedList<Integer>();
		for (int i = 256; i <= 259; i++)
			listOfPossibleRepair.add(i);
		for (int i = 267; i <= 279; i++)
			listOfPossibleRepair.add(i);
		for (int i = 283; i <= 286; i++)
			listOfPossibleRepair.add(i);
		for (int i = 290; i <= 294; i++)
			listOfPossibleRepair.add(i);
		for (int i = 298; i <= 317; i++)
			listOfPossibleRepair.add(i);
	}

	public static ACHelper getInstance() {
		if (instance == null)
			instance = new ACHelper();
		return instance;
	}

	/**
	 * @param pluginInstance
	 *            the pluginInstance to set
	 */
	public void setPluginInstance(AdminCmd pluginInstance) {
		this.pluginInstance = pluginInstance;
		fManager = new FilesManager(pluginInstance.getDataFolder().getPath());
		blacklist = getBlackListedItems();
		alias.putAll(fManager.getAlias());
	}

	/**
	 * @return the pluginInstance
	 */
	public AdminCmd getPluginInstance() {
		return pluginInstance;
	}

	public void setSender(CommandSender player) {
		this.sender = player;
	}

	private void setTime(World w, String arg) {
		long curtime = w.getTime();
		long newtime = curtime - (curtime % 24000);
		if (arg.equalsIgnoreCase("day"))
			newtime += 0;
		else if (arg.equalsIgnoreCase("night"))
			newtime += 14000;
		else if (arg.equalsIgnoreCase("dusk"))
			newtime += 12500;
		else if (arg.equalsIgnoreCase("dawn"))
			newtime += 23000;
		else
			// if not a constant, use raw time
			try {
				newtime += Integer.parseInt(arg);
			} catch (Exception e) {
			}
		w.setTime(newtime);
	}

	// all functions return if they handled the command
	// false then means to show the default handle
	// ! make sure the player variable IS a player!
	// set world time to a new value
	public boolean timeSet(String arg) {
		if (isPlayer(false)) {
			Player p = (Player) sender;
			setTime(p.getWorld(), arg);
		} else {
			for (World w : sender.getServer().getWorlds())
				setTime(w, arg);
		}
		return true;

	}

	public boolean isPlayer() {
		return Utils.isPlayer(sender);
	}

	public boolean isPlayer(boolean msg) {
		return Utils.isPlayer(sender, msg);
	}

	// teleports chosen player to another player

	public boolean tpP2P(String nFrom, String nTo) {
		boolean found = true;
		Player pFrom = sender.getServer().getPlayer(nFrom);
		Player pTo = sender.getServer().getPlayer(nTo);
		if (pFrom == null) {
			sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + nFrom + ChatColor.RED
					+ " not found!");
			found = false;
		}
		if (pTo == null) {
			sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + nTo + ChatColor.RED
					+ " not found!");
			found = false;
		}
		if (found) {
			pFrom.teleport(pTo);
			sender.sendMessage("Successfully teleported " + ChatColor.BLUE + pFrom.getName()
					+ ChatColor.WHITE + " to " + ChatColor.GREEN + pTo.getName());
		}
		return true;
	}

	/**
	 * Add an item to the BlackList
	 * 
	 * @param name
	 * @return
	 */
	public boolean setBlackListedItem(String name) {
		MaterialContainer m = checkMaterial(name);
		if (!m.isNull()) {
			Configuration config = fManager.getFile("blacklist.yml");
			List<Integer> list = config.getIntList("BlackListed", null);
			if (list == null)
				list = new ArrayList<Integer>();
			list.add(m.material.getId());
			config.setProperty("BlackListed", list);
			config.save();
			if (blacklist == null)
				blacklist = new ArrayList<Integer>();
			blacklist.add(m.material.getId());
			sender.sendMessage(ChatColor.GREEN + "Item (" + ChatColor.WHITE + m.material
					+ ChatColor.GREEN + ") added to the Black List.");
			return true;
		}
		return false;
	}

	/**
	 * Set the spawn point.
	 */
	public void setSpawn() {
		if (isPlayer()) {
			Location loc = ((Player) sender).getLocation();
			((Player) sender).getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(),
					loc.getBlockZ());
			spawnLocations.put(loc.getWorld().getName(), loc);
			fManager.setSpawnLoc(loc);
			sender.sendMessage(ChatColor.DARK_GREEN + "SpawnPoint" + ChatColor.WHITE + " set");
		}
	}

	public void spawn() {
		if (isPlayer()) {
			Player player = ((Player) sender);
			Location loc = null;
			String worldName = player.getWorld().getName();
			if (spawnLocations.containsKey(worldName))
				loc = spawnLocations.get(worldName);
			if (loc == null) {
				loc = fManager.getSpawnLoc(worldName);
				if (loc != null)
					spawnLocations.put(worldName, loc);
			}
			if (loc == null)
				loc = player.getWorld().getSpawnLocation();
			player.teleport(loc);
			sender.sendMessage("Teleported to " + ChatColor.DARK_GREEN + "SpawnPoint");
		}
	}

	/**
	 * remove a black listed item
	 * 
	 * @param name
	 * @return
	 */
	public boolean removeBlackListedItem(String name) {
		MaterialContainer m = checkMaterial(name);
		if (m.material != null) {
			Configuration config = fManager.getFile("blacklist.yml");
			List<Integer> list = config.getIntList("BlackListed", null);
			if (list == null)
				list = new ArrayList<Integer>();
			if (!list.isEmpty() && list.contains(m.material.getId())) {
				list.remove((Integer) m.material.getId());
				config.setProperty("BlackListed", list);
				config.save();
			}
			if (blacklist != null && !blacklist.isEmpty() && blacklist.contains(m.material.getId()))
				blacklist.remove((Integer) m.material.getId());
			sender.sendMessage(ChatColor.GREEN + "Item (" + ChatColor.WHITE + m + ChatColor.GREEN
					+ ") removed from the Black List.");
			return true;
		}
		return false;
	}

	/**
	 * Get the blacklisted items
	 * 
	 * @return
	 */
	private List<Integer> getBlackListedItems() {
		return fManager.getFile("blacklist.yml")
				.getIntList("BlackListed", new ArrayList<Integer>());
	}

	/**
	 * Get the user that need to be processed by the command
	 * 
	 * @param args
	 * @param permNode
	 * @param index
	 * @return
	 */
	public Player getUser(String[] args, String permNode, int index) {
		Player target = null;
		if (args.length >= index + 1) {
			if (PermissionManager.getInstance().hasPerm(sender, permNode + ".other"))
				target = sender.getServer().getPlayer(args[index]);
			else
				return target;
		} else if (ACHelper.getInstance().isPlayer(false))
			target = ((Player) sender);
		else {
			sender.sendMessage("You must type the player name");
			return target;
		}
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "Player " + ChatColor.WHITE + args[index]
					+ ChatColor.RED + " not found!");
			return target;
		}
		return target;

	}

	public Player getUser(String[] args, String permNode) {
		return getUser(args, permNode, 0);
	}

	/**
	 * Heal the selected player.
	 * 
	 * @param name
	 * @return
	 */
	public boolean setPlayerHealth(String[] name, String toDo) {
		Player target = getUser(name, "admincmd.player." + toDo + ".other");
		if (target == null)
			return false;
		if (toDo.equals("heal")) {
			target.setHealth(20);
			target.setFireTicks(0);
		} else
			target.setHealth(0);

		return true;
	}

	/**
	 * Translate the id or name to a material
	 * 
	 * @param mat
	 * @return Material
	 */
	public MaterialContainer checkMaterial(String mat) {
		MaterialContainer m = Utils.checkMaterial(mat);
		if (m.isNull())
			sender.sendMessage(ChatColor.RED + "Unknown material: " + ChatColor.WHITE + mat);
		return m;

	}

	public MaterialContainer getAlias(String name) {
		return alias.get(name);
	}

	// ----- / item coloring section -----

	// translates a given color value/name into a real color value
	// also does some checking (error = -1)
	private short getColor(String name, Material mat) {
		short value = -1;
		// first try numbered colors
		try {
			value = Short.parseShort(name);
		} catch (Exception e) {
			// try to find the name then
			for (short i = 0; i < materialsColors.get(mat).length; ++i)
				if (materialsColors.get(mat)[i].equalsIgnoreCase(name)) {
					value = i;
					break;
				}
		}
		// is the value OK?
		if (value < 0 || value >= materialsColors.get(mat).length)
			return -1;
		return value;
	}

	// returns all members of the color name array concatenated with commas
	private String printColors(Material mat) {
		String output = "";
		for (int i = 0; i < materialsColors.get(mat).length; ++i)
			output += materialsColors.get(mat)[i] + ", ";
		return output;
	}

	private void weatherChange(World w, String type, String[] duration) {
		if (type == "clear") {
			w.setThundering(false);
			w.setStorm(false);
			sender.sendMessage(ChatColor.GOLD + "Sky cleared in world : " + w.getName());
		} else {
			if (duration == null || duration.length < 1) {
				w.setStorm(true);
				w.setWeatherDuration(12000);
				sender.sendMessage(ChatColor.GOLD + "Storm set for 10 mins in world : "
						+ w.getName());
			} else {
				try {
					w.setStorm(true);
					int time = Integer.parseInt(duration[0]);
					w.setWeatherDuration(time * 1200);
					sender.sendMessage(ChatColor.GOLD + "Storm set for " + time
							+ " mins in world : " + w.getName());
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.BLUE + "Sorry, that (" + duration[0]
							+ ") isn't a number!");
					w.setStorm(true);
					w.setWeatherDuration(12000);
					sender.sendMessage(ChatColor.GOLD + "Storm set for 10 mins in world : "
							+ w.getName());
				}
			}
		}
	}

	public boolean weather(String type, String[] duration) {
		if (isPlayer(false)) {
			weatherChange(((Player) sender).getWorld(), type, duration);
		} else
			for (World w : sender.getServer().getWorlds())
				weatherChange(w, type, duration);

		return true;
	}

	public void addVulcain(String playerName, float power) {
		addPowerUser("vulcan", playerName, power);
	}

	public void removeVulcan(String playerName) {
		removePowerUser("vulcan", playerName);
	}

	public void addPowerUser(String powerName, String user, Object power) {
		if (usersWithPowers.containsKey(powerName))
			usersWithPowers.get(powerName).put(user, power);
		else {
			ConcurrentMap<String, Object> tmp = new MapMaker().makeMap();
			tmp.put(user, power);
			usersWithPowers.put(powerName, tmp);
		}

	}

	public void addPowerUser(String powerName, Player user, Object power) {
		addPowerUser(powerName, user.getName(), power);
	}

	public void addPowerUser(String powerName, Player user) {
		addPowerUser(powerName, user.getName(), 0);
	}

	public void addPowerUser(String powerName, String user) {
		addPowerUser(powerName, user, 0);
	}

	public void removePowerUser(String powerName, String user) {
		if (usersWithPowers.containsKey(powerName))
			usersWithPowers.get(powerName).remove(user);
		if (usersWithPowers.get(powerName).isEmpty())
			usersWithPowers.remove(powerName);
	}

	public void removePowerUser(String powerName, Player user) {
		removePowerUser(powerName, user.getName());
	}

	public boolean isAPowerUser(String powerName, String user) {
		return usersWithPowers.containsKey(powerName)
				&& usersWithPowers.get(powerName).containsKey(user);
	}

	public Object getPowerOfPowerUser(String powerName, Object user) {
		String player = null;
		if (user instanceof String)
			player = (String) user;
		else if (user instanceof Player)
			player = ((Player) user).getName();
		if (player != null && isAPowerUser(powerName, player))
			return usersWithPowers.get(powerName).get(user);
		return null;

	}

	public List<Player> getAllPowerUser(String power) {
		List<Player> players = new ArrayList<Player>();
		if (usersWithPowers.containsKey(power))
			for (String player : usersWithPowers.get(power).keySet())
				players.add(pluginInstance.getServer().getPlayer(player));
		return players;
	}

	public boolean isAPowerUser(String powerName, Player user) {
		return isAPowerUser(powerName, user.getName());
	}

	public void addGod(String playerName) {
		addPowerUser("god", playerName);
	}

	public void removeGod(String playerName) {
		removePowerUser("god", playerName);
	}

	public void addThor(String playerName) {
		addPowerUser("thor", playerName);
	}

	public void removeThor(String playerName) {
		removePowerUser("thor", playerName);
	}

	public boolean hasThorPowers(String player) {
		return isAPowerUser("thor", player);
	}

	public boolean hasGodPowers(String player) {
		return isAPowerUser("god", player);
	}

	public Float getVulcainExplosionPower(String player) {
		return (Float)getPowerOfPowerUser("vulcan", player);
	}

	public boolean alias(String[] args) {
		MaterialContainer m = checkMaterial(args[1]);
		if (m.isNull())
			return true;
		String alias = args[0];
		this.alias.put(alias, m);
		this.fManager.addAlias(alias, m);
		sender.sendMessage(ChatColor.BLUE + "You can now use " + ChatColor.GOLD + alias
				+ ChatColor.BLUE + " for the item " + ChatColor.GOLD + m.display());
		return true;
	}

	public boolean rmAlias(String alias) {
		this.fManager.removeAlias(alias);
		this.alias.remove(alias);
		sender.sendMessage(ChatColor.GOLD + alias + ChatColor.RED + " removed");
		return true;
	}

	public boolean reparable(int id) {
		return listOfPossibleRepair.contains(id);
	}

	// changes the color of a colorable item in hand
	public boolean itemColor(String color) {
		if (isPlayer()) {
			// help?
			if (color.equalsIgnoreCase("help")) {
				sender.sendMessage(ChatColor.RED + "Wool: " + ChatColor.WHITE
						+ printColors(Material.WOOL));
				sender.sendMessage(ChatColor.RED + "Dyes: " + ChatColor.WHITE
						+ printColors(Material.INK_SACK));
				sender.sendMessage(ChatColor.RED + "Logs: " + ChatColor.WHITE
						+ printColors(Material.LOG));
				sender.sendMessage(ChatColor.RED + "Slab: " + ChatColor.WHITE
						+ printColors(Material.STEP));
				return true;
			}
			// determine the value based on what you're holding
			short value = -1;
			Material m = ((Player) sender).getItemInHand().getType();

			if (materialsColors.containsKey(m))
				value = getColor(color, m);
			else {
				sender.sendMessage(ChatColor.RED + "You must hold a colorable material!");
				return true;
			}
			// error?
			if (value < 0) {
				sender.sendMessage(ChatColor.RED + "Color " + ChatColor.WHITE + color
						+ ChatColor.RED + " is not usable for what you're holding!");
				return true;
			}

			((Player) sender).getItemInHand().setDurability(value);
		}
		return true;
	}

	public boolean inBlackList(MaterialContainer mat) {
		if (!PermissionManager.getInstance().hasPerm(sender, "admincmd.item.noblacklist", false)
				&& blacklist.contains(mat.material.getId())) {
			sender.sendMessage(ChatColor.DARK_RED + "This item (" + ChatColor.WHITE + mat.display()
					+ ChatColor.DARK_RED + ") is black listed.");
			return true;
		}
		return false;
	}

	public boolean inBlackList(ItemStack mat) {
		if (!PermissionManager.getInstance().hasPerm(sender, "admincmd.item.noblacklist", false)
				&& blacklist.contains(mat.getTypeId())) {
			sender.sendMessage(ChatColor.DARK_RED + "This item (" + ChatColor.WHITE + mat.getType()
					+ ChatColor.DARK_RED + ") is black listed.");
			return true;
		}
		return false;
	}

	// ----- / item coloring section -----
}
