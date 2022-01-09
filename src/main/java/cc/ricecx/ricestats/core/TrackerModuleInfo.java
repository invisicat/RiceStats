package cc.ricecx.ricestats.core;

import org.bukkit.Material;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TrackerModuleInfo {
    String name();

    String description() default "No description provided :(";

    Material icon() default Material.BELL;
}
