package id.flowsense;


import com.grack.nanojson.JsonObject;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.*;

public class DonationTriggerManager {
    private final List<DonationTrigger> commandtriggers = new ArrayList<>();
    private final JavaPlugin plugin;

    public DonationTriggerManager(JavaPlugin plugin) {
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
                DonationTrigger commandtrigger = new DonationTrigger(key, amount, commands);
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

        for (DonationTrigger commandtrigger : commandtriggers) {
            if (commandtrigger.shouldTrigger(donationAmount)) {
                commandtrigger.run(placeholders);
            }
        }
    }

    private Map<String, String> buildPlaceholders(com.grack.nanojson.JsonObject jsonObject) {
        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("version", (String) jsonObject.get("version"));
        placeholders.put("created_at", (String) jsonObject.get("created_at"));
        placeholders.put("id", (String) jsonObject.get("id"));
        placeholders.put("type", (String) jsonObject.get("type"));

        int amountRaw = ((Number) jsonObject.get("amount_raw")).intValue();
        int cut = ((Number) jsonObject.get("cut")).intValue();

        placeholders.put("amount_raw", String.valueOf(amountRaw));
        placeholders.put("amount_formatted", formatAmount(amountRaw));
        placeholders.put("cut", String.valueOf(cut));
        placeholders.put("cut_formatted", formatAmount(cut));

        placeholders.put("donator_name", (String) jsonObject.get("donator_name"));
        placeholders.put("donator_email", (String) jsonObject.get("donator_email"));
        placeholders.put("donator_is_user", String.valueOf(jsonObject.get("donator_is_user")));
        placeholders.put("message", (String) jsonObject.get("message"));

        return placeholders;
    }


    private String formatAmount(int amount) {
        return String.format("%,d", amount);
    }

    public List<DonationTrigger> getcommandtriggers() {
        return commandtriggers;
    }
}
