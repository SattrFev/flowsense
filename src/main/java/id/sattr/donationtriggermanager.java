package id.sattr;

import com.google.gson.JsonObject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;

public class donationtriggermanager {
    private final List<donationtrigger> commandtriggers = new ArrayList<>();
    private final JavaPlugin plugin;

    public donationtriggermanager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadFromDonationTriggerConfig() {

        File donationFile = new File(plugin.getDataFolder(), "donationtrigger.yml");

        if (!donationFile.exists()) {
            plugin.saveResource("donationtrigger.yml", false);  // Salin file ke folder data
        }

        FileConfiguration donationConfig = YamlConfiguration.loadConfiguration(donationFile);

        Set<String> keys = donationConfig.getConfigurationSection("").getKeys(false);
        for (String key : keys) {
            String amount = donationConfig.getString(key + ".amount");
            List<String> commands = donationConfig.getStringList(key + ".commands");

            if (amount != null && commands != null && !commands.isEmpty()) {
                donationtrigger commandtrigger = new donationtrigger(key, amount, commands);
                commandtriggers.add(commandtrigger);
            }
        }
    }

    public void clearCommands() {
        commandtriggers.clear();
    }

    public void checkAndRun(int donationAmount, JsonObject jsonObject) {
        // Map placeholders to replace variables in commands
        Map<String, String> placeholders = buildPlaceholders(jsonObject);

        for (donationtrigger commandtrigger : commandtriggers) {
            if (commandtrigger.shouldTrigger(donationAmount)) {
                commandtrigger.run(placeholders);
            }
        }
    }

    private Map<String, String> buildPlaceholders(JsonObject jsonObject) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("version", jsonObject.get("version").getAsString());
        placeholders.put("created_at", jsonObject.get("created_at").getAsString());
        placeholders.put("id", jsonObject.get("id").getAsString());
        placeholders.put("type", jsonObject.get("type").getAsString());
        placeholders.put("amount_raw", String.valueOf(jsonObject.get("amount_raw").getAsInt()));
        placeholders.put("amount_formatted", formatAmount(jsonObject.get("amount_raw").getAsInt()));
        placeholders.put("cut", String.valueOf(jsonObject.get("cut").getAsInt()));
        placeholders.put("cut_formatted", formatAmount(jsonObject.get("cut").getAsInt()));
        placeholders.put("donator_name", jsonObject.get("donator_name").getAsString());
        placeholders.put("donator_email", jsonObject.get("donator_email").getAsString());
        placeholders.put("donator_is_user", String.valueOf(jsonObject.get("donator_is_user").getAsBoolean()));
        placeholders.put("message", jsonObject.get("message").getAsString());

        return placeholders;
    }

    private String formatAmount(int amount) {
        return String.format("%,d", amount);
    }

    public List<donationtrigger> getcommandtriggers() {
        return commandtriggers;
    }
}
