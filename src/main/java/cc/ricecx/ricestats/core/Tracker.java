package cc.ricecx.ricestats.core;

import cc.ricecx.ricestats.RiceStats;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.influxdb.dto.Point;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public abstract class Tracker implements Listener {

    private final Logger logger = RiceStats.getInstance().getLogger();

    private static final Map<Class<? extends Event>, List<MethodPair>> eventMap = new HashMap<>();


    public Tracker() {
        boolean hasListener = false;
        for (Method declaredMethod : this.getClass().getDeclaredMethods()) {
            if(declaredMethod.isAnnotationPresent(EventHandler.class)) {
                hasListener = true;
                continue;
            }

            if(!declaredMethod.isAnnotationPresent(TrackerInfo.class)) continue;
            TrackerInfo trackerInfo = declaredMethod.getAnnotation(TrackerInfo.class);

            logger.info("Tracking stat: " + trackerInfo.name());

            addTrackerToEvent(trackerInfo.event(), new MethodPair(this, declaredMethod));
        }

        if(hasListener) Bukkit.getPluginManager().registerEvents(this, RiceStats.getInstance());

        logger.info("Registered tracker " + this.getClass().getSimpleName() + " to " + eventMap.size() + " events");
    }

    private static void addTrackerToEvent(Class<? extends Event> event, MethodPair methodPair) {
        if(eventMap.containsKey(event)) {
            eventMap.get(event).add(methodPair);
        } else {
            List<MethodPair> p = new ArrayList<MethodPair>();
            p.add(methodPair);
            eventMap.put(event,  p);
        }
    }

    public static void registerAllTrackers() {
        for (Class<? extends Event> event : eventMap.keySet()) {
            Bukkit.getServer().getPluginManager().registerEvent(event, new TrackingEmptyListener(), EventPriority.MONITOR, (listener, event1) -> {
                if(eventMap.get(event1.getClass()) == null) return;
                for (MethodPair methodPair : eventMap.get(event1.getClass())) {
                    try {

                        methodPair.execute(event1);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }, RiceStats.getInstance());
        }
    }


    protected void addTracker(Point point) {
        RiceStats.getInstance().getInfluxDB().write(point);
    }

    private static final class TrackingEmptyListener implements Listener { }
}
