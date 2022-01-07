package cc.ricecx.ricestats.commands.subcommands;


import cc.ricecx.ricestats.core.command.SubCommand;
import cc.ricecx.ricestats.core.command.SubCommandInfo;
import org.bukkit.command.CommandSender;

@SubCommandInfo(
        name = "migratevanilla"
)
public class MigrateVanillaCommand implements SubCommand {

    @Override
    public void execute(CommandSender sender, String[] args) {
        System.out.println("migrating vanilla");
    }
}
