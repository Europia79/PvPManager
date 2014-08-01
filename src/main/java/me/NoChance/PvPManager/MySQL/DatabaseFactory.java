package me.NoChance.PvPManager.MySQL;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseFactory {
	private final JavaPlugin plugin;
	
	public DatabaseFactory(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Construct a database
	 * 
	 * @param builder Database configuration
	 * @return New database
	 */
	public Database getDatabase(DatabaseConfigBuilder builder) {
		return new Database(this, builder);
	}
	
	/**
	 * Generates a default config section with the name of MySQL (defaults to sqlite)
	 */
	public void generateConfigSection() {
		FileConfiguration config = plugin.getConfig();
		config.addDefault("MySQL.enabled", false);
		config.addDefault("MySQL.host", "127.0.0.1");
		config.addDefault("MySQL.port", 3306);
		config.addDefault("MySQL.user", "root");
		config.addDefault("MySQL.password", "INSERT PASSWORD");
		config.addDefault("MySQL.database", plugin.getName());
		config.options().copyDefaults(true);
		plugin.saveConfig();
	}
	
}