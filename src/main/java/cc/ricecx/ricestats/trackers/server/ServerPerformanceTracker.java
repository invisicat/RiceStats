package cc.ricecx.ricestats.trackers.server;

import cc.ricecx.ricestats.RiceStats;
import cc.ricecx.ricestats.core.Tracker;
import cc.ricecx.ricestats.core.TrackerModuleInfo;
import cc.ricecx.ricestats.utils.CpuMonitor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.influxdb.dto.Point;

@TrackerModuleInfo(name = "Server Performance Tracker")
public class ServerPerformanceTracker extends Tracker {

    public ServerPerformanceTracker() {
        super();
        startTasks();
    }

    private void startTasks() {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                reportCurrentTickrate();
            }
        };

        runnable.runTaskTimerAsynchronously(RiceStats.getInstance(), 0, 20 * 3);

        RiceStats.getInstance().getLogger().info("Server performance tracker started");
    }

    private void reportCurrentTickrate() {
        addTracker(Point.measurement("server")
                .addField("tickrate_1m", Bukkit.getTPS()[0])
                .addField("tickrate_5m", Bukkit.getTPS()[1])
                .addField("tickrate_15m", Bukkit.getTPS()[2])
                .addField("averageMspt", Bukkit.getAverageTickTime())
                .addField("available_cores", Runtime.getRuntime().availableProcessors())
                .addField("cpu_usage", CpuMonitor.processLoad())
                .addField("cpu_10s", CpuMonitor.processLoad10SecAvg())
                .addField("cpu_1m", CpuMonitor.processLoad1MinAvg())
                .addField("cpu_15m", CpuMonitor.processLoad15MinAvg())
                .addField("total_memory", Runtime.getRuntime().totalMemory())
                .addField("free_memory", Runtime.getRuntime().freeMemory())
                .addField("max_memory", Runtime.getRuntime().maxMemory())
                .addField("current_players", Bukkit.getOnlinePlayers().size())
                .addField("max_players", Bukkit.getMaxPlayers())
                .build());
    }
}
