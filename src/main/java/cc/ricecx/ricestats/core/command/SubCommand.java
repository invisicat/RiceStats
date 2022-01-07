package cc.ricecx.ricestats.core.command;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface SubCommand {

    void execute(CommandSender sender, String[] args);

    default List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of("");
    }
}
