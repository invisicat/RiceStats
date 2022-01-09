package cc.ricecx.ricestats.trackers.player;

import cc.ricecx.ricestats.core.Tracker;
import cc.ricecx.ricestats.core.TrackerInfo;
import cc.ricecx.ricestats.core.TrackerModuleInfo;
import cc.ricecx.ricestats.core.event.VillagerTradeListener;
import org.bukkit.entity.Player;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.raid.RaidSpawnWaveEvent;
import org.bukkit.event.raid.RaidStopEvent;
import org.bukkit.event.raid.RaidTriggerEvent;
import org.influxdb.dto.Point;

import java.util.concurrent.TimeUnit;

@TrackerModuleInfo(name = "PlayerVillagerTracker", description = "Track player villager interactions")
public class PlayerVillagerTracker extends Tracker {

    @TrackerInfo(name = "Villager Trade", description = "Listens for when the player trades with a villager", event = VillagerTradeListener.VillagerTradeEvent.class)
    public void onVillagerTrade(VillagerTradeListener.VillagerTradeEvent evt) {
        Player player = (Player) evt.getPlayer();


        addTracker(Point.measurement("villager_traded")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("player", player.getName())
                .tag("uuid", player.getUniqueId().toString())
                .tag("ingredient", evt.getBestNameForIngredientOne())
                .tag("ingredient2", evt.getBestNameForIngredientTwo() != null ? evt.getBestNameForIngredientTwo() : "")
                .addField("item_bought_amount", evt.getAmountPurchased())
                .addField("item_bought", evt.getBestNameForResultItem())
                .addField("ingr1_item", evt.getBestNameForIngredientOne())
                .addField("ingr1_amt", evt.getIngredientOneTotalAmount())
                .addField("ingr2_item", evt.getBestNameForIngredientTwo() != null ? evt.getBestNameForIngredientTwo() : "")
                .addField("ingr2_amt", evt.getIngredientTwoTotalAmount())
                .build());
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
