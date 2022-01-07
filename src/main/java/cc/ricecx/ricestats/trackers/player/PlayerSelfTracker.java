package cc.ricecx.ricestats.trackers.player;

import cc.ricecx.ricestats.core.Tracker;
import cc.ricecx.ricestats.core.TrackerInfo;
import cc.ricecx.ricestats.core.TrackerModuleInfo;
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.influxdb.dto.Point;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@TrackerModuleInfo(name = "PlayerSelfTracker", description = "Track player stats")
public class PlayerSelfTracker extends Tracker {

    private final Map<UUID, Long> playerTime = new HashMap<>();


    @TrackerInfo(name = "Player Gather Experience", description = "Track player experience", event = PlayerPickupExperienceEvent.class)
    public void onExperienceGather(PlayerPickupExperienceEvent evt) {
        Player player = evt.getPlayer();
        addTracker(Point.measurement("experience_gathered")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", player.getName())
                .tag("uuid", player.getUniqueId().toString())
                .addField("experience_gathered", evt.getExperienceOrb().getExperience())
                .build());
    }

    @TrackerInfo(name = "Player Death", description = "Track player death", event = PlayerDeathEvent.class)
    public void onPlayerDeath(PlayerDeathEvent evt) {
        addTracker(Point.measurement("player_death")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", evt.getEntity().getName())
                .tag("uuid", evt.getEntity().getUniqueId().toString())
                .tag("death_causes", evt.getEntity().getLastDamageCause().getCause().toString())
                .addField("death_time", System.currentTimeMillis())
                .addField("uuid", evt.getEntity().getUniqueId().toString())
                .addField("cause", evt.getEntity().getLastDamageCause().getCause().toString())
                .build());
    }

    @TrackerInfo(name = "Player Craft", description = "Track player craft", event = CraftItemEvent.class)
    public void onPlayerCraft(CraftItemEvent evt) {
        HumanEntity entity = evt.getWhoClicked();
        if(evt.getCurrentItem() == null) return;
        addTracker(Point.measurement("player_craft")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", entity.getName())
                .tag("uuid", entity.getUniqueId().toString())
                .tag("item", evt.getCurrentItem().getType().toString())
                .addField("item_crafted", evt.getCurrentItem().getType().toString())
                .addField("item_amount", evt.getCurrentItem().getAmount())
                .build()
        );
    }

    @TrackerInfo(name = "Player Eat", description = "Track what the player eats", event = PlayerItemConsumeEvent.class)
    public void onPlayerEat(PlayerItemConsumeEvent evt) {
        Player player = evt.getPlayer();
        addTracker(Point.measurement("food_eaten")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", player.getName())
                .tag("uuid", player.getUniqueId().toString())
                .tag("food", evt.getItem().getType().toString())
                .addField("food_eaten", evt.getItem().getType().toString())
                .build());
    }

    @TrackerInfo(name = "Ingame Time", description = "Tracks how long the player has been on the server", event = PlayerJoinEvent.class)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        playerTime.put(evt.getPlayer().getUniqueId(), System.currentTimeMillis());
    }


    @TrackerInfo(name = "Player Join", description = "Track player join", event = PlayerJoinEvent.class)
    public void perPlayerJoin(PlayerJoinEvent evt) {
        addTracker(Point.measurement("player_join")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", evt.getPlayer().getName())
                .tag("uuid", evt.getPlayer().getUniqueId().toString())
                .addField("join_time", System.currentTimeMillis())
                .addField("uuid", evt.getPlayer().getUniqueId().toString())
                .build());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent evt) {
        if(!playerTime.containsKey(evt.getPlayer().getUniqueId())) return;
        addTracker(Point.measurement("time_played")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", evt.getPlayer().getName())
                .tag("uuid", evt.getPlayer().getUniqueId().toString())
                .addField("join_time", playerTime.get(evt.getPlayer().getUniqueId()))
                .addField("leave_time", System.currentTimeMillis())
                .addField("elapsed_ms", System.currentTimeMillis() - playerTime.get(evt.getPlayer().getUniqueId()))
                .build());

        playerTime.remove(evt.getPlayer().getUniqueId());
    }

    @TrackerInfo(name = "Change World", description = "Players changing worlds", event = PlayerChangedWorldEvent.class)
    public void onChangeWorld(PlayerChangedWorldEvent evt) {
        addTracker(Point.measurement("world_change")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", evt.getPlayer().getName())
                .tag("uuid", evt.getPlayer().getUniqueId().toString())
                .tag("dimension", ifNull(evt.getPlayer().getWorld().getName(), "Overworld"))
                .addField("from", ifNull(evt.getFrom().getName(), "Overworld"))
                .addField("to", ifNull(evt.getPlayer().getWorld().getName(), "Nether"))
                .build());
    }

    private String ifNull(String s, String def) {
        if(s == null) return def;
        return s;
    }
}
