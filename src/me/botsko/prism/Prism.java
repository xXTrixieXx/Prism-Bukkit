package me.botsko.prism;

import java.sql.Connection;
import java.util.logging.Logger;

import me.botsko.prism.db.Mysql;
import me.botsko.prism.listeners.PrismBlockBreakEvent;
import me.botsko.prism.recorders.ActionRecorder;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Prism extends JavaPlugin {

	protected String msg_name = "Mythos";
	protected Logger log = Logger.getLogger("Minecraft");
	public FileConfiguration config;
	protected Language language;
	public Connection conn = null;
	
	public ActionRecorder actionRecorder;
	
	
    /**
     * Enables the plugin and activates our player listeners
     */
	@Override
	public void onEnable(){
		
		this.log("Initializing plugin. By Viveleroi (and team), Darkhelmet Minecraft: s.dhmc.us");
		
//		try {
//		    Metrics metrics = new Metrics(this);
//		    metrics.start();
//		} catch (IOException e) {
//		    log("MCStats submission failed.");
//		}
		
		// Load configuration, or install if new
		loadConfig();
		
		// Assign event listeners
		getServer().getPluginManager().registerEvents(new PrismBlockBreakEvent( this ), this);
		
		// Add commands
//		getCommand("dm").setExecutor( (CommandExecutor) new DmCommandExecutor(this) );
		
		actionRecorder = new ActionRecorder(this);
		
	}
	
	
	/**
	 * Load configuration and language files
	 */
	public void loadConfig(){
		PrismConfig mc = new PrismConfig( this );
		config = mc.getConfig();
		// Load language files
		language = new Language( this, mc.getLang() );
	}
	
	
	/**
     * Setup a generic connection all non-scheduled methods may share
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @return true if we successfully connected to the db.
     */
	public void dbc(){
		Mysql mysql = new Mysql(
				config.getString("prism.mysql.username"), 
				config.getString("prism.mysql.password"), 
				config.getString("prism.mysql.hostname"), 
				config.getString("prism.mysql.database"), 
				config.getString("prism.mysql.port")
		);
		conn = mysql.getConn();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Language getLang(){
		return this.language;
	}
	
	
	/**
	 * 
	 * @param msg
	 * @return
	 */
	public String playerMsg(String msg){
		if(msg != null){
			return ChatColor.GOLD + "["+msg_name+"]: " + ChatColor.WHITE + msg;
		}
		return "";
	}
	
	
	/**
	 * 
	 * @param msg
	 * @return
	 */
	public String playerError(String msg){
		if(msg != null){
			return ChatColor.GOLD + "["+msg_name+"]: " + ChatColor.RED + msg;
		}
		return "";
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public void log(String message){
		log.info("["+msg_name+"]: " + message);
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public void debug(String message){
		if(this.config.getBoolean("prism.debug")){
			log.info("["+msg_name+"]: " + message);
		}
	}
	
	
	/**
	 * Shutdown
	 */
	@Override
	public void onDisable(){
		this.log("Closing plugin.");
	}
}
