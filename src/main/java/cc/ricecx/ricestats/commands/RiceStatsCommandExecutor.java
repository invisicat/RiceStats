package cc.ricecx.ricestats.commands;

import cc.ricecx.ricestats.core.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RiceStatsCommandExecutor implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command _command, @NotNull String label, String[] args) {
        if (args.length == 0) return false;

        SubCommand subCommand = Commands.fromName(args[0]);

        if (subCommand == null) return false;

        subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Commands.getCommandNames(), completions);
            //sort the list
            Collections.sort(completions);
            return completions;
        }

        SubCommand subCommand = Commands.fromName(args[0]);

        if (subCommand == null) return null;

        return subCommand.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
    }
}
