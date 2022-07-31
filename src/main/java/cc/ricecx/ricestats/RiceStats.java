package cc.ricecx.ricestats;

import cc.ricecx.ricestats.commands.RiceStatsCommandExecutor;
import cc.ricecx.ricestats.core.Tracker;
import cc.ricecx.ricestats.trackers.player.*;
import cc.ricecx.ricestats.trackers.server.ServerPerformanceTracker;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;


public final class RiceStats extends JavaPlugin {

    private final RiceStatsCommandExecutor mainCommand = new RiceStatsCommandExecutor();

    private InfluxDB influxDB;

    @Override
    public void onEnable() {
        registerConfig();
        registerCommand();
        try {
            initializeInflux();
        } catch (Exception e) {
            getLogger().severe("Failed to initialize InfluxDB");
            getLogger().severe(e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
        registerTrackers();
    }

    private void registerConfig() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }

    private void initializeInflux() {
        final String serverURL = this.getConfig().getString("influx.host"), username = this.getConfig().getString("influx.username"), password = this.getConfig().getString("influx.password");

        if (serverURL == null || username == null)
            throw new RuntimeException("InfluxDB configuration is missing!");

        influxDB = InfluxDBFactory.connect(serverURL, username, password);

        String databaseName = this.getConfig().getString("influx.database") != null ? this.getConfig().getString("influx.database") : "rice_stats";

        if (!influxDB.ping().isGood())
            throw new RuntimeException("Could not connect to InfluxDB! Please check your InfluxDB credentials.");

        influxDB.query(new Query("CREATE DATABASE " + databaseName));
        influxDB.setDatabase(databaseName);

        influxDB.enableBatch(
                BatchOptions.DEFAULTS.threadFactory(runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setDaemon(true);
                    return thread;
                })
        );
        Runtime.getRuntime().addShutdownHook(new Thread(influxDB::close));
    }

    private void registerCommand() {
        PluginCommand command = getCommand("ricestats");
        if (command == null) throw new RuntimeException("Could not hook with rice stats command!");
        command.setExecutor(mainCommand);
        command.setTabCompleter(mainCommand);
    }

    private void registerTrackers() {
        new ServerPerformanceTracker();
        new PlayerBlockTracker();
        new PlayerAnimalTracker();
        new PlayerMonsterTracker();
        new PlayerSelfTracker();
        new PlayerVillagerTracker();

        Tracker.registerAllTrackers();
    }

    public InfluxDB getInfluxDB() {
        return influxDB;
    }

    @Override
    public void onDisable() {
        if (influxDB != null) influxDB.close();
    }

    public static RiceStats getInstance() {
        return getPlugin(RiceStats.class);
    }
}
