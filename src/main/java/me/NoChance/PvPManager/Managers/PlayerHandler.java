package me.NoChance.PvPManager.Managers;

import java.util.HashMap;
import java.util.UUID;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.PvPlayer;
import me.NoChance.PvPManager.Config.Variables;
import me.NoChance.PvPManager.MySQL.Database;
import me.NoChance.PvPManager.Tasks.CleanKillersTask;

public class PlayerHandler {

	private HashMap<String, PvPlayer> players = new HashMap<String, PvPlayer>();
	private ConfigManager configManager;
	private PvPManager plugin;
	private Database database;
	private Economy economy;

	public PlayerHandler(PvPManager plugin) {
		this.plugin = plugin;
		this.configManager = plugin.getConfigM();
		this.database = plugin.getDataBase();
		if (Variables.killAbuseEnabled)
			new CleanKillersTask(this).runTaskTimer(plugin, 1200, Variables.killAbuseTime * 20);
		if (Variables.fineEnabled || Variables.playerKillsEnabled) {
			if (plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
				if (setupEconomy()) {
					plugin.getLogger().info("Vault Found! Using it for currency related features");
				} else
					plugin.getLogger().severe("Error! No Economy plugin found");
			} else {
				plugin.getLogger().severe("Vault not found! Features requiring Vault won't work!");
				Variables.fineEnabled = false;
			}
		}
		addOnlinePlayers();
	}

	private void addOnlinePlayers() {
		for (Player p : plugin.getServer().getOnlinePlayers()) {
			add(p);
		}
	}

	public PvPlayer get(Player player) {
		String name = player.getName();
		return players.containsKey(name) ? players.get(name) : add(player);
	}

	private PvPlayer add(Player player) {
		PvPlayer pvPlayer = new PvPlayer(player, plugin);
		players.put(player.getName(), pvPlayer);
		checkPlayerData(pvPlayer.getUUID());
		return pvPlayer;
	}

	public void remove(final PvPlayer player) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable() {
			public void run() {
				if (player.getPlayer() == null) {
					players.remove(player.getName());
				}
			}
		}, 1800);
		savePvPState(player.getUUID(), player.hasPvPEnabled());
	}

	private void checkPlayerData(final UUID uuid) {
		new BukkitRunnable() {

			public void run() {
				database.connect();
				if (!database.exists(uuid.toString()))
					database.addPlayerEntry(uuid.toString());
				database.close();
			}

		}.runTaskAsynchronously(plugin);
	}

	public void addKill(final UUID id) {
		new BukkitRunnable() {

			public void run() {
				database.connect();
				database.increment("kills", id.toString());
				database.close();
			}

		}.runTaskAsynchronously(plugin);
	}
	
	public void addDeath(final UUID id) {
		new BukkitRunnable() {

			public void run() {
				database.connect();
				database.increment("deaths", id.toString());
				database.close();
			}

		}.runTaskAsynchronously(plugin);
	}

	public void savePvPState(UUID id, boolean pvpState) {
		configManager.saveUser(id, !pvpState);
	}

	private void applyFine(Player p) {
		if (economy != null) {
			economy.withdrawPlayer(p.getName(), Variables.fineAmount);
		} else {
			plugin.getLogger().severe("Tried to apply fine but no Economy plugin found!");
			plugin.getLogger().severe("Disable fines feature or get an Economy plugin to fix this error");
		}
	}

	public void giveReward(String name) {
		if (economy != null) {
			economy.depositPlayer(name, Variables.moneyReward);
		} else {
			plugin.getLogger().severe("Tried to give reward but no Economy plugin found!");
			plugin.getLogger().severe("Disable money reward on kill or get an Economy plugin to fix this error");
		}
	}

	public void applyPunishments(Player player) {
		PvPlayer pvPlayer = get(player);
		if (Variables.killOnLogout) {
			pvPlayer.setPvpLogged(true);
			ItemStack[] inventory = null;
			ItemStack[] armor = null;
			if (!Variables.dropInventory || !Variables.dropArmor) {
				if (!Variables.dropInventory) {
					inventory = player.getInventory().getContents();
					player.getInventory().clear();
				}
				if (!Variables.dropArmor) {
					armor = player.getInventory().getArmorContents();
					player.getInventory().setArmorContents(null);
				}
			}
			player.setHealth(0);
			player.setHealth(20);
			if (inventory != null)
				player.getInventory().setContents(inventory);
			if (armor != null)
				player.getInventory().setArmorContents(armor);
		} else if (!Variables.killOnLogout) {
			if (Variables.dropInventory) {
				fakeInventoryDrop(player, player.getInventory().getContents());
				player.getInventory().clear();
			}
			if (Variables.dropArmor) {
				fakeInventoryDrop(player, player.getInventory().getArmorContents());
				player.getInventory().setArmorContents(null);
			}
			if (Variables.dropExp)
				fakeExpDrop(player);
		}
		if (Variables.fineEnabled)
			applyFine(player);
	}

	private void fakeInventoryDrop(Player player, ItemStack[] inventory) {
		Location playerLocation = player.getLocation();
		World playerWorld = player.getWorld();
		for (ItemStack itemstack : inventory) {
			if (itemstack != null && !itemstack.getType().equals(Material.AIR))
				playerWorld.dropItemNaturally(playerLocation, itemstack);
		}
	}

	private void fakeExpDrop(Player player) {
		int expdropped = player.getLevel() * 7;
		if (expdropped < 100)
			player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(expdropped);
		else
			player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(100);
		player.setLevel(0);
		player.setExp(0);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}

	public HashMap<String, PvPlayer> getPlayers() {
		return players;
	}

	public PvPManager getPlugin() {
		return plugin;
	}

}
