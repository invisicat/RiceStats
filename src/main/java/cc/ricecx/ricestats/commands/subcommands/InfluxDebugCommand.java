package cc.ricecx.ricestats.commands.subcommands;

import cc.ricecx.ricestats.RiceStats;
import cc.ricecx.ricestats.core.command.SubCommand;
import cc.ricecx.ricestats.core.command.SubCommandInfo;
import org.bukkit.command.CommandSender;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

@SubCommandInfo(
        name = "influxdebug"
)
public class InfluxDebugCommand implements SubCommand {
    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("InfluxDB Debug");

        String query = String.join(" ", args);

        QueryResult result = RiceStats.getInstance().getInfluxDB().query(new Query(query));
        System.out.println(result);
    }
}
