package me.botsko.prism.commands;

import com.helion3.prism.libs.elixr.ChunkUtils;
import me.botsko.prism.Prism;
import me.botsko.prism.commandlibs.CallInfo;
import me.botsko.prism.commandlibs.SubHandler;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ViewCommand implements SubHandler {

    /**
	 * 
	 */
    private final Prism plugin;

    /**
     * 
     * @param plugin
     * @return
     */
    public ViewCommand(Prism plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle the command
     */
    @Override
    public void handle(CallInfo call) {

        final String playerName = call.getPlayer().getName();

        /*
          View current chunk boundaries
         */
        if( call.getArg( 1 ).equals( "chunk" ) ) {

            // Do they already have a view?
            if( plugin.playerActiveViews.containsKey( playerName ) ) {

                // Get bounding blocks
                final ArrayList<Block> blocks = plugin.playerActiveViews.get( playerName );

                // Reset to current
                ChunkUtils.resetPreviewBoundaryBlocks( call.getPlayer(), blocks );

                // Close
                call.getSender().sendMessage( Prism.messenger.playerHeaderMsg( "Reset your current view." ) );
                plugin.playerActiveViews.remove( playerName );

            } else {

                // Get bounding blocks
                final ArrayList<Block> blocks = ChunkUtils.getBoundingBlocksAtY( call.getPlayer().getLocation()
                        .getChunk(), call.getPlayer().getLocation().getBlockY() );

                // Set preview blocks
                ChunkUtils.setPreviewBoundaryBlocks( call.getPlayer(), blocks, Material.GLOWSTONE );
                plugin.playerActiveViews.put( playerName, blocks );

                call.getSender().sendMessage( Prism.messenger.playerHeaderMsg( "Showing current chunk boundaries." ) );

            }
            return;
        }

        call.getSender().sendMessage( Prism.messenger.playerError( "Invalid view option. Use /prism ? for help." ) );

    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }
}