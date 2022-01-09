package cc.ricecx.ricestats.trackers.player;

import cc.ricecx.ricestats.core.Tracker;
import cc.ricecx.ricestats.core.TrackerInfo;
import cc.ricecx.ricestats.core.TrackerModuleInfo;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.influxdb.dto.Point;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@TrackerModuleInfo(name = "PlayerAnimalTracker", description = "Track player animal stats")
public class PlayerAnimalTracker extends Tracker {

    private final Map<UUID, Long> mountMap = new HashMap<>();

    @TrackerInfo(name = "player_animal_breed", description = "Track player animal breed", event = EntityBreedEvent.class)
    public void onAnimalBreed(EntityBreedEvent evt) {
        if (evt.getBreeder() instanceof Player player) {
            addTracker(Point.measurement("animal_breed")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("player", player.getName())
                    .tag("uuid", player.getUniqueId().toString())
                    .tag("entity", evt.getEntity().getType().toString())
                    .addField("entity_bred", evt.getEntity().getType().toString())
                    .build());
        }
    }

    @TrackerInfo(name = "player_animal_sheep_dye", description = "Track player animal sheep dye", event = SheepDyeWoolEvent.class)
    public void onSheepDyeEvent(SheepDyeWoolEvent evt) {
        if (evt.getPlayer() == null) return;
        addTracker(Point.measurement("sheep_dye")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", evt.getPlayer().getName())
                .tag("uuid", evt.getPlayer().getUniqueId().toString())
                .tag("color", evt.getColor().name())
                .addField("color", evt.getColor().name())
                .addField("hex", evt.getColor().getColor().asRGB())
                .build());
    }

    @TrackerInfo(name = "player_animal_leash", description = "Track player when they leash an animal", event = PlayerLeashEntityEvent.class)
    public void onPlayerLeadEvent(PlayerLeashEntityEvent evt) {
        addTracker(Point.measurement("leash_entity")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", evt.getPlayer().getName())
                .tag("uuid", evt.getPlayer().getUniqueId().toString())
                .addField("entity", evt.getEntity().getType().toString())
                .build());
    }

    @TrackerInfo(name = "player_animal_fish", description = "Track player on fish caught", event = PlayerFishEvent.class)
    public void onPlayerFish(PlayerFishEvent evt) {
        if (evt.getState() == PlayerFishEvent.State.CAUGHT_FISH && evt.getCaught() instanceof Fish fish) {
            addTracker(Point.measurement("fish_caught")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("player", evt.getPlayer().getName())
                    .tag("uuid", evt.getPlayer().getUniqueId().toString())
                    .tag("fish", fish.getType().toString())
                    .addField("fish_caught", fish.getType().toString())
                    .build());
        }
    }

    @TrackerInfo(name = "player_animal_mount", description = "Track player animal mount", event = EntityMountEvent.class)
    public void onEntityMount(EntityMountEvent evt) {
        if (evt.getMount() instanceof Player player) {
            mountMap.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onEntityDismount(EntityDismountEvent evt) {
        if (evt.getDismounted() instanceof Player player) {
            EntityType entity = evt.getEntity().getType();

            // InfluxDB
            addTracker(Point.measurement("mount")
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .tag("player", player.getName())
                    .tag("uuid", player.getUniqueId().toString())
                    .tag("entity", entity.toString())
                    .addField("mounted_entity", entity.toString())
                    .addField("time_start", mountMap.get(player.getUniqueId()))
                    .addField("elapsed_on", System.currentTimeMillis() - mountMap.get(player.getUniqueId()))
                    .build());
            /*               */
            mountMap.remove(player.getUniqueId());
        }
    }
}
