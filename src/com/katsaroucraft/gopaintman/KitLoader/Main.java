package com.katsaroucraft.gopaintman.KitLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {
	List<String> kits;
	static File invFile = null;
	static FileConfiguration invFileConfig = null;
	File kFileDir = null;

	@Override
	public void onEnable() {
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);
		this.getConfig().options().copyDefaults(true);
		this.saveDefaultConfig();
		getServer().getLogger();
		this.kits = this.getConfig().getStringList("kits");

		kFileDir = new File(this.getDataFolder(), "Kits");
		if (!kFileDir.exists()) {
			kFileDir.mkdir();
		}
	}

	@Override
	public void onDisable() {
		getServer().getLogger();

	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		return true;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent ev) {
		Player p = ev.getPlayer();
		if (ev.getLine(0).contains("[SaveKit]")) {
			p.sendMessage(ChatColor.GREEN
					+ "KitLoader Sign successfully created");
		} else if (ev.getLine(0).contains("[LoadKit]")) {
			p.sendMessage(ChatColor.RED + "KitLoader Sign successfully created");
		}

	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onSignHit(PlayerInteractEvent ev) {
		// Check if player hits a sign with the prefix in it
		if (ev.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if ((ev.getClickedBlock().getType().equals(Material.SIGN_POST) || ev
					.getClickedBlock().getType().equals(Material.WALL_SIGN))) {
				Sign sign = (Sign) ev.getClickedBlock().getState();
				if (sign.getLine(0).contains("[SaveKit]")) {
					if (!sign.getLine(1).equals(null)
							&& !sign.getLine(1).equalsIgnoreCase("")) {

						if (saveInventoryToFile(ev.getPlayer().getInventory(),
								kFileDir, sign.getLine(1))) {
							ev.getPlayer().sendMessage(
									ChatColor.GREEN + "Saving kit");
						} else {
							ev.getPlayer().sendMessage(
									ChatColor.RED + "Could not save Kit!");

						}

					} else {
						ev.getPlayer().sendMessage(
								ChatColor.RED + "You need to enter in a name");
					}
				} else if (sign.getLine(0).contains("[LoadKit]")) {
					if (!sign.getLine(1).equals(null)
							&& !sign.getLine(1).equalsIgnoreCase("")) {
						Inventory inv = getInventoryFromFile(new File(kFileDir,
								sign.getLine(1) + ".invsave"));
						if (inv != null) {

							ev.getPlayer().getInventory()
									.setContents(inv.getContents());

							ev.getPlayer().sendMessage(
									ChatColor.GREEN + "Kit loaded!");
						} else {
							ev.getPlayer().sendMessage(
									ChatColor.RED + "Kit could not be loaded!");
						}

					} else {
						ev.getPlayer().sendMessage(
								ChatColor.GREEN + "Inventory loaded!");
					}
				}

			}
		}
	}

	public Player getPlayer(String name) {
		name = name.toLowerCase();
		for (Player player : Bukkit.getOnlinePlayers()) {

			if (player.getName().toLowerCase().contains(name)
					|| player.getDisplayName().toLowerCase().contains(name)) {
				return player;
			}
		}
		return null;
	}

	public static boolean saveInventoryToFile(Inventory inventory, File path,
			String fileName) {
		if (inventory == null || path == null || fileName == null)
			return false;
		try {
			File invFile = new File(path, fileName + ".invsave");
			if (invFile.exists())
				invFile.delete();
			FileConfiguration invConfig = YamlConfiguration
					.loadConfiguration(invFile);

			invConfig.set("Title", inventory.getTitle());
			invConfig.set("Size", inventory.getSize());
			invConfig.set("Max stack size", inventory.getMaxStackSize());
			if (inventory.getHolder() instanceof Player)
				invConfig.set("Holder",
						((Player) inventory.getHolder()).getName());

			ItemStack[] invContents = inventory.getContents();
			for (int i = 0; i < invContents.length; i++) {
				ItemStack itemInInv = invContents[i];
				if (itemInInv != null)
					if (itemInInv.getType() != Material.AIR)
						invConfig.set("Slot " + i, itemInInv);
			}

			invConfig.save(invFile);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static Inventory getInventoryFromFile(File file) {
		if (file == null)
			return null;
		if (!file.exists() || file.isDirectory()
				|| !file.getAbsolutePath().endsWith(".invsave"))
			return null;
		try {
			FileConfiguration invConfig = YamlConfiguration
					.loadConfiguration(file);
			Inventory inventory = null;
			String invTitle = invConfig.getString("Title", "Inventory");
			int invSize = invConfig.getInt("Size", 27);
			int invMaxStackSize = invConfig.getInt("Max stack size", 64);
			InventoryHolder invHolder = null;
			if (invConfig.contains("Holder"))
				invHolder = Bukkit.getPlayer(invConfig.getString("Holder"));
			inventory = Bukkit.getServer().createInventory(invHolder, invSize,
					ChatColor.translateAlternateColorCodes('&', invTitle));
			inventory.setMaxStackSize(invMaxStackSize);
			try {
				ItemStack[] invContents = new ItemStack[invSize];
				for (int i = 0; i < invSize; i++) {
					if (invConfig.contains("Slot " + i))
						invContents[i] = invConfig.getItemStack("Slot " + i);
					else
						invContents[i] = new ItemStack(Material.AIR);
				}
				inventory.setContents(invContents);
			} catch (Exception ex) {
			}
			return inventory;
		} catch (Exception ex) {
			return null;
		}
	}

}