package cc.ricecx.ricestats.trackers.player;

import cc.ricecx.ricestats.core.Tracker;
import cc.ricecx.ricestats.core.TrackerInfo;
import cc.ricecx.ricestats.core.TrackerModuleInfo;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;


@TrackerModuleInfo(name = "Player Blocks")
public class PlayerBlockTracker extends Tracker {

    @TrackerInfo(name = "Block Break", description = "Blocks broken by players", event = BlockBreakEvent.class)
    public void onBlockBreak(BlockBreakEvent evt) {
        addTracker(Point.measurement("blocks_broken")
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .tag("player", evt.getPlayer().getName())
                        .tag("uuid", evt.getPlayer().getUniqueId().toString())
                        .tag("block", evt.getBlock().getType().toString())
                        .addField("block", evt.getBlock().getType().toString())
                        .addField("y", evt.getBlock().getY())
                .build());
    }

    @TrackerInfo(name = "Block Place", description = "Blocks placed by players", event = BlockPlaceEvent.class)
    public void onBlockPlace(BlockPlaceEvent evt) {
        addTracker(Point.measurement("blocks_placed")
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .tag("player", evt.getPlayer().getName())
                        .tag("uuid", evt.getPlayer().getUniqueId().toString())
                        .tag("block", evt.getBlock().getType().toString())
                        .addField("block", evt.getBlock().getType().toString())
                        .addField("y", evt.getBlock().getY())
                        .build());
    }
}
