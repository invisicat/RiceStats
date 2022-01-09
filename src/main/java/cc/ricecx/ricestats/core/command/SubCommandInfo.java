package cc.ricecx.ricestats.core.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommandInfo {
    String name();

    String description() default "No description provided :(";

}
