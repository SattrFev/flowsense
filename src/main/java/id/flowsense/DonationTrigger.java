package id.flowsense;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import static id.flowsense.Flowsense.prefix;
import static id.flowsense.Flowsense.loggx;

public class DonationTrigger {
    private final String key;
    private final String amountCondition;
    private final List<String> commands;
    private boolean isValid = true;
    private final Logger logger = Bukkit.getLogger();
    private int threshold = 0;
    private String operator = "=";

    public DonationTrigger(String key, String rawCondition, List<String> commands) {
        this.key = key;
        this.commands = commands;
        String cleaned = rawCondition.replaceAll("\\s+", "");

        if (!cleaned.startsWith(">") && !cleaned.startsWith("<") && !cleaned.startsWith("=")) {
            operator = "=";
            logger.warning("[saweriaBridge] Warning: condition for '" + key + "' has no operator, assuming '='");
            cleaned = "=" + cleaned;
        } else {
            operator = cleaned.substring(0, 1);
            if (!cleaned.equals(rawCondition)) {
                logger.warning("[saweriaBridge] Warning: condition for '" + key + "' contains unnecessary spaces. Cleaned to: " + cleaned);
            }
        }

        amountCondition = cleaned;

        try {
            threshold = Integer.parseInt(cleaned.substring(operator.equals("=") ? 1 : 1));
        } catch (NumberFormatException e) {
            logger.severe("[saweriaBridge] Error: invalid amount condition for '" + key + "': '" + rawCondition + "'");
            isValid = false;
        }
    }

    public boolean shouldTrigger(int donationAmount) {
        if (!isValid) return false;
        switch (operator) {
            case ">":
                return donationAmount > threshold;
            case "<":
                return donationAmount < threshold;
            case "=":
                return donationAmount == threshold;
            default:
                return false;
        }
    }

    public void run(Map<String, String> placeholders) {
        if (!isValid) {
            loggx(prefix + " &eSkipping command '" + key + "' due to invalid amount condition.");
            return;
        }

        for (String command : commands) {
            String processedCommand = command;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                processedCommand = processedCommand.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
    }

    public String getKey() {
        return key;
    }

    public List<String> getCommands() {
        return commands;
    }
}
