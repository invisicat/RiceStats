package cc.ricecx.ricestats.commands;

import cc.ricecx.ricestats.commands.subcommands.InfluxDebugCommand;
import cc.ricecx.ricestats.commands.subcommands.MigrateVanillaCommand;
import cc.ricecx.ricestats.core.command.SubCommand;
import cc.ricecx.ricestats.core.command.SubCommandInfo;

import java.util.ArrayList;
import java.util.List;

public enum Commands {
    MIGRATE_VANILLA(new MigrateVanillaCommand()),
    INFLUX_DEBUG(new InfluxDebugCommand()),
    ;



    private static final Commands[] CACHE = values();

    private final SubCommand command;

    Commands(SubCommand cmd) {
        this.command = cmd;
    }


    public static SubCommand fromName(String name) {
        for (Commands cmd : CACHE) {
            if (cmd.getInfo().name().equalsIgnoreCase(name)) {
                return cmd.command;
            }
        }

        return null;
    }

    public static Commands[] getSubCommands() {
        return CACHE;
    }

    public static List<String> getCommandNames() {
        List<String> names = new ArrayList<>();
        for (Commands commands : CACHE) {
            names.add(commands.getInfo().name());
        }
        return names;
    }

    public SubCommandInfo getInfo() {
        return command.getClass().getAnnotation(SubCommandInfo.class);
    }
}
