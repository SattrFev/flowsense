package id.sattr;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static id.sattr.flowsense.*;

public class commands implements CommandExecutor, TabCompleter {
    private final flowsense plugin;
    public commands(flowsense plugin) {
        this.plugin = plugin;
        plugin.getCommand("flowsense").setExecutor(this);
        plugin.getCommand("flowsense").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("flowsense.reload")) {
                sendColored(sender,PREFIXE + "&cyou don't have permission to use this command.");
                return true;
            }
            sendColored(sender,PREFIXE + " &ereloading flowsense...");
            loggx( PREFIXE + "&cReloading flowsense...");
            plugin.reloadConfig();
            plugin.commandtriggermanager.clearCommands();
            plugin.commandtriggermanager.loadFromDonationTriggerConfig();
            plugin.restartWebhookServer(sender);

            return true;
        }

        return false;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                completions.add("reload");
            }
            return completions;
        }

        return Collections.emptyList();
    }
}

