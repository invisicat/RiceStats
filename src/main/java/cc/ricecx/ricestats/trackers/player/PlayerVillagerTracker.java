package cc.ricecx.ricestats.trackers.player;

import cc.ricecx.ricestats.core.Tracker;
import cc.ricecx.ricestats.core.TrackerInfo;
import cc.ricecx.ricestats.core.TrackerModuleInfo;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.bukkit.inventory.ItemStack;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

@TrackerModuleInfo(name = "PlayerVillagerTracker", description = "Track player villager interactions")
public class PlayerVillagerTracker extends Tracker {

    @TrackerInfo(name = "Villager Trade", description = "Listens for when the player trades with a villager", event = PlayerTradeEvent.class)
    public void onVillagerTrade(PlayerTradeEvent evt) {
        Player player = evt.getPlayer();

        Point.Builder point = Point.measurement("villager_traded")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", player.getName())
                .tag("uuid", player.getUniqueId().toString());

        for (int i = 0; i < evt.getTrade().getIngredients().size(); i++) {
            ItemStack item = evt.getTrade().getIngredients().get(i);
            point.addField("ingredient" + i, item.getType().name());
            point.addField("ingredient_amt_" + i, item.getAmount());
        }

        point.addField("result", evt.getTrade().getResult().getType().name());
        point.addField("result_amt", evt.getTrade().getResult().getAmount());

        addTracker(point.build());

    }

    @TrackerInfo(name = "Raid Finish", description = "Listens for when the player finishes a raid", event = RaidFinishEvent.class)
    public void onRaidSucceed(RaidFinishEvent evt) {

    }

    @TrackerInfo(name = "Raid Fail", description = "Listens for when the player fails a raid", event = RaidStopEvent.class)
    public void onRaidFail(RaidStopEvent evt) {
        if (!evt.getReason().equals(RaidStopEvent.Reason.NOT_IN_VILLAGE)) return;
    }

    @TrackerInfo(name = "Raid Waves", description = "Listens for when the player completes a raid wave", event = RaidSpawnWaveEvent.class)
    public void onRaidWave(RaidSpawnWaveEvent evt) {

    }

    @TrackerInfo(name = "Raid Start", description = "Listens for when the player starts a raid", event = RaidTriggerEvent.class)
    public void onRaidStart(RaidSpawnWaveEvent evt) {
    }

}
