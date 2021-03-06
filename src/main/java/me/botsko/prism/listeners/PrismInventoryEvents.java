package me.botsko.prism.listeners;

import java.util.Map;
import java.util.Map.Entry;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionFactory;
import me.botsko.prism.actionlibs.RecordingQueue;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class PrismInventoryEvents implements Listener {

    /**
	 * 
	 */
    private final Prism plugin;

    /**
     * 
     * @param plugin
     */
    public PrismInventoryEvents(Prism plugin) {
        this.plugin = plugin;
    }

    /**
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryPickupItem(final InventoryPickupItemEvent event) {

        if( !plugin.getConfig().getBoolean( "prism.track-hopper-item-events" ) )
            return;

        if( !Prism.getIgnore().event( "item-pickup" ) )
            return;

        // If hopper
        if( event.getInventory().getType().equals( InventoryType.HOPPER ) ) {
            RecordingQueue.addToQueue( ActionFactory.createItemStack("item-pickup", event.getItem().getItemStack(), event
                    .getItem().getItemStack().getAmount(), -1, null, event.getItem().getLocation(), "hopper") );
        }
    }

    /**
     * Handle inventory transfers
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(final InventoryDragEvent event) {

        if( !plugin.getConfig().getBoolean( "prism.tracking.item-insert" )
                && !plugin.getConfig().getBoolean( "prism.tracking.item-remove" ) )
            return;

        // Get container
        final InventoryHolder ih = event.getInventory().getHolder();
        Location containerLoc = null;
        if( ih instanceof BlockState ) {
            final BlockState eventChest = (BlockState) ih;
            containerLoc = eventChest.getLocation();
        }

        // Store some info
        final Player player = (Player) event.getWhoClicked();

        final Map<Integer, ItemStack> newItems = event.getNewItems();
        for ( final Entry<Integer, ItemStack> entry : newItems.entrySet() ) {
            recordInvAction( player, containerLoc, entry.getValue(), entry.getKey(), "item-insert" );
        }
    }

    /**
     * Handle inventory transfers
     * 
     * @param event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {

        if( !plugin.getConfig().getBoolean( "prism.tracking.item-insert" )
                && !plugin.getConfig().getBoolean( "prism.tracking.item-remove" ) )
            return;

        Location containerLoc = null;

        // Store some info
        final Player player = (Player) event.getWhoClicked();
        final ItemStack currentitem = event.getCurrentItem();
        final ItemStack cursoritem = event.getCursor();

        // Get location
        if( event.getInventory().getHolder() instanceof BlockState ) {
            final BlockState b = (BlockState) event.getInventory().getHolder();
            containerLoc = b.getLocation();
        } else if( event.getInventory().getHolder() instanceof Entity ) {
            final Entity e = (Entity) event.getInventory().getHolder();
            containerLoc = e.getLocation();
        } else if( event.getInventory().getHolder() instanceof DoubleChest ) {
            final DoubleChest chest = (DoubleChest) event.getInventory().getHolder();
            containerLoc = chest.getLocation();
        }

        // Double chests report 27 default size, though they actually
        // have 6 rows of 9 for 54 slots
        int defaultSize = event.getView().getType().getDefaultSize();
        if( event.getInventory().getHolder() instanceof DoubleChest ){
            defaultSize = event.getView().getType().getDefaultSize() * 2;
        }

        // Click in the block inventory produces slot/rawslot that are equal, only until the slot numbers exceed the
        // slot count of the inventory. At that point, they represent the player inv.
        if( event.getSlot() == event.getRawSlot() && event.getRawSlot() <= defaultSize ) {
        	ItemStack itemadded = null;
        	ItemStack itemremoved = null;

        	if( currentitem != null && !currentitem.getType().equals( Material.AIR ) && cursoritem != null
                    && !cursoritem.getType().equals( Material.AIR ) ) {
                // If BOTH items are not air and they can't be stacked, then you've swapped an item.
            	// We need to record an insert for the cursor item and and remove for the current.
        		if (currentitem.isSimilar(cursoritem)) {
        			// Items are the same and can be stacked, check if there is space for stacking (and how much)
        			int qty = cursoritem.getAmount();
        			int free = (currentitem.getMaxStackSize() - currentitem.getAmount());
        			int inserted = (qty <= free) ? qty : free;
        			if (inserted > 0) {
        				// Make sure we only record the amount of items actually inserted from the cursor stack
        				itemadded = cursoritem.clone();
        				itemadded.setAmount(inserted);
        			}

        			// Right click only inserts 1 item at a time so don't add the full itemstack amount
                	if (event.isRightClick()) {
                		if (itemadded != null) itemadded.setAmount(1);
                	}
        		} else {
        			// Clicked/Held items are not the same, they have been swapped!
        			itemremoved = currentitem.clone();
        			itemadded = cursoritem.clone();
        		}
            } else if( currentitem != null && !currentitem.getType().equals( Material.AIR ) ) {
            	// Clicked slot is not AIR, so player picked it up
            	itemremoved = currentitem.clone();
            } else if( cursoritem != null && !cursoritem.getType().equals( Material.AIR ) ) {
            	// Held item is not AIR, so played inserted item
                itemadded = cursoritem.clone();
            }

        	// Now record the events in the DB
        	if (itemadded != null) {
                recordInvAction( player, containerLoc, itemadded, event.getRawSlot(), "item-insert", event );
        	}
        	if (itemremoved != null) {
                recordInvAction( player, containerLoc, itemremoved, event.getRawSlot(), "item-remove", event );
        	}
            return;
        }
        if( event.isShiftClick() && cursoritem != null && cursoritem.getType().equals( Material.AIR ) ) {
            recordInvAction( player, containerLoc, currentitem, -1, "item-insert", event );
        }
    }

    /**
     * 
     * @param player
     * @param item
     * @param slot
     * @param actionType
     */
    protected void recordInvAction(Player player, Location containerLoc, ItemStack item, int slot, String actionType) {
        recordInvAction( player, containerLoc, item, slot, actionType, null );
    }

    /**
     * 
     * @param player
     * @param item
     * @param slot
     * @param actionType
     */
    protected void recordInvAction(Player player, Location containerLoc, ItemStack item, int slot, String actionType,
            InventoryClickEvent event) {
        if( !Prism.getIgnore().event( actionType, player ) )
            return;

        // Determine correct quantity. Right-click events change the item
        // quantity but don't seem to update the cursor/current items.
        int officialQuantity = 0;
        if( item != null ) {
            officialQuantity = item.getAmount();
            // If the player right-clicked we need to assume the amount
            if( event != null && event.isRightClick() ) {
                // If you're right-clicking to remove an item, it divides by two
                if( actionType.equals( "item-remove" ) ) {
                    officialQuantity = ( officialQuantity - (int) Math.floor( ( item.getAmount() / 2 ) ) );
                }
                // If you're right-clicking to insert, it's only one
                else if( actionType.equals( "item-insert" ) ) {
                    officialQuantity = 1;
                }
            }
        }

        // Record it!
        if( actionType != null && containerLoc != null && item != null && item.getTypeId() != 0 && officialQuantity > 0 ) {
            RecordingQueue.addToQueue( ActionFactory.createItemStack(actionType, item, officialQuantity, slot, null,
                    containerLoc, player.getName()) );
        }
    }
}
