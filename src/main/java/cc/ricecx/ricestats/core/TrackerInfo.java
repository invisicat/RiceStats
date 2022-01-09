package cc.ricecx.ricestats.core;

import org.bukkit.event.Event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TrackerInfo {
    String name();
    String description();
    Class<? extends Event> event();
    String id() default "null"; // so retarded
}
