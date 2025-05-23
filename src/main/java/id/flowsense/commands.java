package id.flowsense;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static id.flowsense.flowsense.*;
import static java.lang.System.getLogger;

public class commands implements CommandExecutor, TabCompleter {
    private final flowsense plugin;
    donationtriggermanager commandtriggermanager;
    public commands(flowsense plugin) {
        this.plugin = plugin;
        plugin.getCommand("flowsense").setExecutor(this);
        plugin.getCommand("flowsense").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("flowsense.reload")) {
                sendColored(sender, "&cyou don't have permission to use this command.");
                return true;
            }
            sendColored(sender, prefix + " &ereloading flowsense...");
            loggx(prefix + "&cReloading flowsense...");
            plugin.reloadConfig();
            plugin.commandtriggermanager.clearCommands();
            plugin.commandtriggermanager.loadFromDonationTriggerConfig();
            plugin.restartWebhookServer(sender);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("fakedonate")) {
            if (!sender.hasPermission("flowsense.fakedonate")) {
                sendColored(sender, "&cyou don't have permission to use this command.");
                return true;
            }

            if (args.length < 3 || args.length > 5) {
                sendColored(sender, prefix + " &cUsage: /flowsense fakedonate <donatorname> <amount> [message] [cut]");
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sendColored(sender, prefix + " &cAmount must be a valid number.");
                return true;
            }

            double cut;
            try {
                cut = (args.length > 4) ? Double.parseDouble(args[4]) : 0;
            } catch (NumberFormatException e) {
                sendColored(sender, prefix + " &cCut must be a valid number.");
                return true;
            }

            String donator = args[1];
            String message = (args.length > 3) ? args[3] : " ";


            int cutAmount = (int) (amount * (cut / 100.0));

            String formatted = ZonedDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            JsonObject obj = new JsonObject();
            obj.put("version", "2022.01");
            obj.put("created_at", formatted);
            obj.put("id", "00000000-0000-0000-0000-000000000000");
            obj.put("type", "donation");
            obj.put("amount_raw", amount);
            obj.put("cut", cutAmount);
            obj.put("donator_name", donator);
            obj.put("donator_email", donator + "@example.com");
            obj.put("donator_is_user", false);
            obj.put("message", message);
            JsonObject etc = new JsonObject();
            etc.put("amount_to_display", amount);
            obj.put("etc", etc);
            Bukkit.getPluginManager().callEvent(new donationevent(JsonWriter.string(obj)));
            if (consolelog) loggx(prefix + " &dDonation Recieved!");
            if (isCommandTrigger) {
                plugin.commandtriggermanager.checkAndRun(amount, obj);
            };
            if (isMessage) {
                plugin.handleMessage(obj);
            };
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
            if ("fakedonate".startsWith(args[0].toLowerCase())) {
                completions.add("fakedonate");
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
