package me.botsko.prism.actionlibs;

import java.util.ArrayList;
import java.util.HashMap;

import me.botsko.prism.actions.ActionType;
import me.botsko.prism.appliers.PrismProcessType;

import org.bukkit.Location;

public class QueryParameters implements Cloneable {
	
	// Internal use
	protected HashMap<String,String> foundArgs = new HashMap<String,String>();
	protected PrismProcessType lookup_type = PrismProcessType.LOOKUP;
	
	// Typically required
	protected int radius;
	protected String world;
	
	// Optional
	protected ArrayList<ActionType> action_types = new ArrayList<ActionType>();
	protected Location specific_block_loc;
	protected Location player_location;
	protected int id = 0;
	protected String player_name;
	protected String time_since;
	protected String entity_filters;
	protected ArrayList<String> block_filters = new ArrayList<String>();
	protected boolean allow_no_radius = false;
	protected int limit = 1000000;
	
	// Informational
	protected String original_command;
	
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	
	/**
	 * @return the entity
	 */
	public String getEntity() {
		return entity_filters;
	}


	/**
	 * @param entity the entity to set
	 */
	public void setEntity(String entity) {
		this.entity_filters = entity;
	}


	/**
	 * @return the block
	 */
	public ArrayList<String> getBlockFilters() {
		return block_filters;
	}


	/**
	 * @param block the block to set
	 */
	public void setBlockFilters(ArrayList<String> blocks) {
		this.block_filters = blocks;
	}
	
	
	/**
	 * @param block the block to set
	 */
	public void addBlockFilter(String block) {
		this.block_filters.add(block);
	}
	
	
	/**
	 * @return the loc
	 */
	public Location getSpecificBlockLocation() {
		return specific_block_loc;
	}
	
	
	/**
	 * @param loc the loc to set
	 */
	public void setSpecificBlockLocation(Location loc) {
		this.specific_block_loc = loc;
	}
	
	
	/**
	 * @return the player_location
	 */
	public Location getPlayerLocation() {
		return player_location;
	}


	/**
	 * @param player_location the player_location to set
	 */
	public void setPlayerLocation(Location player_location) {
		this.player_location = player_location;
	}


	/**
	 * @return the radius
	 */
	public int getRadius() {
		return radius;
	}
	
	
	/**
	 * @param radius the radius to set
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}
	
	
	/**
	 * @return the allow_no_radius
	 */
	public boolean getAllow_no_radius() {
		return allow_no_radius;
	}


	/**
	 * @param allow_no_radius the allow_no_radius to set
	 */
	public void setAllow_no_radius(boolean allow_no_radius) {
		this.allow_no_radius = allow_no_radius;
	}


	/**
	 * @return the player
	 */
	public String getPlayer() {
		return player_name;
	}
	
	
	/**
	 * @param player the player to set
	 */
	public void setPlayer(String player) {
		this.player_name = player;
	}
	
	
	/**
	 * @return the world
	 */
	public String getWorld() {
		return world;
	}
	
	
	/**
	 * @param world the world to set
	 */
	public void setWorld(String world) {
		this.world = world;
	}
	
	
	/**
	 * @return the action_type
	 */
	public ArrayList<ActionType> getActionTypes() {
		return action_types;
	}
	
	
	/**
	 * @param action_type the action_type to set
	 */
	public void addActionType(ActionType action_type) {
		this.action_types.add(action_type);
	}
	
	
	/**
	 * 
	 */
	public void resetActionTypes() {
		action_types.clear();
	}
	
	
	/**
	 * @return the time
	 */
	public String getTime() {
		return time_since;
	}
	
	
	/**
	 * @param time the time to set
	 */
	public void setTime(String time) {
		this.time_since = time;
	}


	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}


	/**
	 * @param limit the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}


	/**
	 * @return the lookup_type
	 */
	public PrismProcessType getLookup_type() {
		return lookup_type;
	}


	/**
	 * @return the foundArgs
	 */
	public HashMap<String, String> getFoundArgs() {
		return foundArgs;
	}


	/**
	 * @param foundArgs the foundArgs to set
	 */
	public void setFoundArgs(HashMap<String, String> foundArgs) {
		this.foundArgs = foundArgs;
	}


	/**
	 * @param lookup_type the lookup_type to set
	 */
	public void setLookup_type(PrismProcessType lookup_type) {
		this.lookup_type = lookup_type;
	}
	
	
	/**
	 * If we're doing a lookup, we want the most
	 * recent actions available. However, if we're
	 * doing a rollback we want them applied in order.
	 * 
	 * @return
	 */
	public String getSortDirection(){
		if(this.lookup_type.equals("lookup")){
			return "DESC";
		}
		return "ASC";
	}
	
	
	/**
	 * This just provides easy access to whether or not any action
	 * type we're searching for should also trigger a restore
	 * of any events afterwards.
	 * 
	 * @param at
	 * @return
	 */
	public boolean shouldTriggerRestoreFor( ActionType at ){
		if(!getActionTypes().isEmpty()){
			for(ActionType requestedType : getActionTypes()){
				if(requestedType.shouldTriggerRestoreFor( at )){
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * This just provides easy access to whether or not any action
	 * type we're searching for should also trigger a rollback
	 * of any events afterwards.
	 * 
	 * @param at
	 * @return
	 */
	public boolean shouldTriggerRollbackFor( ActionType at ){
		if(!getActionTypes().isEmpty()){
			for(ActionType requestedType : getActionTypes()){
				if(requestedType.shouldTriggerRollbackFor( at )){
					return true;
				}
			}
		}
		return false;
	}
	
	
	/**
	 * 
	 * @param args
	 */
	public void setStringFromRawArgs( String[] args ){
		String params = "";
		if(args.length > 0){
			for(int i = 1; i < args.length; i++){
				params += " "+args[i];
			}
		}
		original_command = params;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getOriginalCommand(){
		return original_command;
	}
	
	
	/**
	 * 
	 */
	@Override
	public QueryParameters clone() throws CloneNotSupportedException {
		QueryParameters cloned = (QueryParameters) super.clone();
		cloned.action_types = new ArrayList<ActionType>(action_types);
		return cloned;
	}
}
