package me.NoChance.PvPManager.MySQL;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class Database {
	private static final String MYSQL_URL_TEMPLATE = "jdbc:mysql://%s/%s?user=%s&password=%s";
	private static final String SQLITE_URL_TEMPLATE = "jdbc:sqlite:%s";
	private final String connectionUrl;
	private final String driver;
	private Connection connection;
	private Table table;
	
	protected Database(DatabaseFactory databaseFactory, DatabaseConfigBuilder builder) {
		this.driver = builder.getDriver();
		if(builder.getFile() != null) {
			//Use SQLITE
			this.connectionUrl = String.format(SQLITE_URL_TEMPLATE, builder.getFile());
		} else {
			//Use MYSQL
			this.connectionUrl = String.format(MYSQL_URL_TEMPLATE, builder.getUrl(), builder.getDatabase(), builder.getUser(), builder.getPassword());
		}
	}
	
	/**
	 * Register a table.
	 * 
	 * @param table Table to register.
	 */
	public void registerTable(Table table) {
		try {
			PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + table.getName() + table.getUsage());
			ps.executeUpdate();
			this.table = table;
		} catch (SQLException e) {
			log("Failed to register table", e);
		}
	}
	
	/**
	 * Delete a table
	 * 
	 * @param table Name of table
	 */
	public void deleteTable() {
		try {
			PreparedStatement ps = connection.prepareStatement("DROP TABLE " + table);
			ps.executeUpdate();
		} catch (SQLException e) {
			log("Failed to delete table", e);
		}
	}
	
	/**
	 * Rename a table
	 * 
	 * @param oldName Old name
	 * @param newName New name
	 */
	public void renameTable(String oldName, String newName) {
		try {
			PreparedStatement ps = connection.prepareStatement("RENAME " + oldName + " TO " + newName);
			ps.executeUpdate();
		} catch (SQLException e) {
			log("Failed to rename table", e);
		}
	}
	
	/**
	 * Check if a certain table exists
	 * 
	 * @param table Name of table
	 * @return Table exists?
	 */
	public boolean tableExists(String table) {
		try {
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet result = metadata.getTables(null, null, table, null);
			return result.next();
		} catch (SQLException e) {
			log("Failed to check database", e);
			return false;
		}
	}
	
	/**
	 * Check if a certain column in a table exists
	 * 
	 * @param table Name of table
	 * @param column Name of column
	 * @return Column exists?
	 */
	public boolean columnExists(String table, String column) {
		try {
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet result = metadata.getColumns(null, null, table, column);
			return result.next();
		} catch (SQLException e) {
			log("Failed to check database", e);
			return false;
		}
	}
	
	/**
	 * Insert new data to the database
	 * 
	 * @param table Table to insert data in
	 * @param values Values to insert
	 */
	public void set(Object... values) {
		try {
			String valueCount = "";
			for(int i = 0; i < values.length; i++) {
				valueCount += "?";
				if(i < (values.length - 1))
					valueCount += ",";
			}
			String query = "INSERT INTO " + table.getName() + table.getValues() + " VALUES(" + valueCount + ");";
			PreparedStatement ps = connection.prepareStatement(query);
			for(int i = 0; i < values.length; i++) {
				ps.setObject(i + 1, values[i]);
			}
			
			ps.executeUpdate();
		} catch (SQLException e) {
			log("Failed to insert data to database", e);
		}
	}
	
	public void addPlayerEntry(String id) {
		try {
			PreparedStatement ps = connection.prepareStatement("INSERT INTO users (uuid) VALUES(?);");
			ps.setString(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			log("Failed to insert data to database", e);
		}
	}
	
	public void increment(String toUpdate, String id){
		try {
			PreparedStatement ps = connection.prepareStatement("UPDATE users SET " + toUpdate + "=" + toUpdate + 1 + " WHERE uuid = ?;");
			ps.setObject(1, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			log("Failed to update database", e);
		}
	}
	
	public void executeQuery(String query) {
		try {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} catch (SQLException e) {
			log("Failed to insert data to database", e);
		}
	}
	
	public void printTopThree(){
		try {
			PreparedStatement ps = connection.prepareStatement("select kills,uuid from users order by kills desc limit 3");
			ResultSet r = ps.executeQuery();
			while(r.next()){
				System.out.println(r.getInt("kills"));
				System.out.println(r.getString("uuid"));
			}
		} catch (SQLException e) {
			log("Failed to insert data to database", e);
		}
	}

	/**
	 * Get a value from a table.
	 * 
	 * @param table Table to get value from
	 * @param index Column to search with.
	 * @param toGet Value of column to look for
	 * @param value The value to search with.
	 * @return Value of found, NULL if not.
	 */
	public Object get(String index, String toGet, Object value) {
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + table.getName() + " WHERE " + index + "=?;");
			ps.setObject(1, value);
			ResultSet result = ps.executeQuery();
			if(result.next()) {
				return result.getObject(toGet);
			}
		} catch (SQLException e) {
			log("Failed to get data from database", e);
		}
		
		return null;
	}

	/**
	 * Check if a value exists in the database
	 * 
	 * @param table Table to check from.
	 * @param index Index of the value
	 * @param value Value of the index
	 * @return TRUE if found, FALSE if not
	 */
	public boolean exists(String id) {
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM users WHERE uuid = ?;");
			ps.setObject(1, id);
			ResultSet result = ps.executeQuery();
			return result.next();
		} catch (SQLException e) {
			log("Failed to check database", e);
			return false;
		}
	}

	/**
	 * Update a value in the database
	 * 
	 * @param table Table to update
	 * @param index Index to search with
	 * @param toUpdate Index to update
	 * @param indexValue Value to swarch with.
	 * @param updateValue New value to update.
	 */
	public void update(String index, String toUpdate, Object indexValue, Object updateValue) {
		try {
			PreparedStatement ps = connection.prepareStatement("UPDATE " + table.getName() + " SET " + toUpdate + "=? WHERE " + index + "=?;");
			ps.setObject(1, updateValue);
			ps.setObject(2, indexValue);
			ps.executeUpdate();
		} catch (SQLException e) {
			log("Failed to update database", e);
		}
	}

	/**
	 * Remove data from the database
	 * 
	 * @param table Table to remove from.
	 * @param index Index to search with.
	 * @param value Value to search with.
	 */
	public void remove(String index, Object value) {
		try {
			PreparedStatement ps = connection.prepareStatement("DELTE FROM " + table.getName() + " WHERE " + index + "=?;");
			ps.setObject(1, value);
			ps.executeUpdate();
		} catch (SQLException e) {
			log("Failed to remove from database", e);
		}
	}
	
	/**
	 * Returns the database connection.
	 * 
	 * @return Database connection.
	 */
	public Connection getConnection() {
		return connection;
	}
	
	/**
	 * Connects to database.
	 * 
	 * @throws SQLException
	 */
	public void connect() {
		try {
			Class.forName(driver);
		} catch(Throwable t) {
			log("Failed to load database driver", t);
			return;
		}
		
		try {
			this.connection = DriverManager.getConnection(connectionUrl);
		} catch (SQLException e) {
			log("Failed to connect to database", e);
		}
	}
	
	/**
	 * Checks if the database is currently connected.
	 * 
	 * @return Is database connected?
	 */
	public boolean isConnected() {
		return connection != null;
	}
	
	/**
	 * Closes the database.
	 */
	public void close() {
		try {
			if(isConnected()) {
				connection.close();
				this.connection = null;
			}
		} catch(SQLException e) {
			log("Failed to close database", e);
		}
	}
	
	private void log(String message, Throwable t) {
		Bukkit.getLogger().log(Level.SEVERE, message, t);
	}
}