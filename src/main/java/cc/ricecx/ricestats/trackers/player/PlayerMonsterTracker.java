package cc.ricecx.ricestats.trackers.player;

import cc.ricecx.ricestats.core.Tracker;
import cc.ricecx.ricestats.core.TrackerInfo;
import cc.ricecx.ricestats.core.TrackerModuleInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

@TrackerModuleInfo(name = "PlayerMonsterTracker", description = "Track player monsters")
public class PlayerMonsterTracker extends Tracker {

    @TrackerInfo(name = "player_monster_killed", description = "Track player monster killed", event = EntityDeathEvent.class)
    public void onMobKill(EntityDeathEvent evt) {
        if (evt.getEntity().getKiller() != null) {
            Player player = evt.getEntity().getKiller();

            addTracker(Point.measurement("monster_killed")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("player", player.getName())
                    .tag("uuid", player.getUniqueId().toString())
                    .tag("entity", evt.getEntity().getType().toString())
                    .addField("entity_killed", evt.getEntity().getType().toString())
                    .build());
        }
    }
}
