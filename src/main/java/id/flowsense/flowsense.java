package id.flowsense;


import com.grack.nanojson.JsonObject;
import io.github.milkdrinkers.colorparser.ColorParser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class flowsense extends JavaPlugin {

    private static BukkitAudiences audiences;
    private static flowsense instance;
    private static FlowAuth flowAuth;
    donationtriggermanager commandtriggermanager;

    public static String prefix, token, clientid, prtoken;
    public static int provider;
    public static boolean isCommandTrigger, isMessage, consolelog;


    public static void loggx(String s) {
        Component msg = ColorParser.of(s).parseLegacy().build();
        audiences.console().sendMessage(msg);
    }

    public static void sendColored(CommandSender sender, String message) {
        Component msg = ColorParser.of(message).parseLegacy().build();
        audiences.sender(sender).sendMessage(msg);
    }

    public void handleMessage(JsonObject objx) {
        try {
            String version = (String) objx.get("version");
            String createdAt = (String) objx.get("created_at");
            String idx = (String) objx.get("id");
            String type = (String) objx.get("type");
            int amountRaw = ((Number) objx.get("amount_raw")).intValue();
            int cut = ((Number) objx.get("cut")).intValue();

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
            DecimalFormat f = new DecimalFormat("#,###", symbols);
            String amountFormatted = f.format(amountRaw);
            String cutFormatted = f.format(cut);

            String donatorName = (String) objx.get("donator_name");
            String donatorEmail = (String) objx.get("donator_email");
            String messagex = (String) objx.get("message");
            boolean isDonatorUser = (boolean) objx.get("donator_is_user");

            String messageTemplate = getConfig().getString("message", "");

            String parsed = messageTemplate
                    .replace("{version}", version)
                    .replace("{created_at}", createdAt)
                    .replace("{id}", idx)
                    .replace("{type}", type)
                    .replace("{amount_raw}", String.valueOf(amountRaw))
                    .replace("{amount_formatted}", amountFormatted)
                    .replace("{cut}", String.valueOf(cut))
                    .replace("{cut_formatted}", cutFormatted)
                    .replace("{donator_name}", donatorName)
                    .replace("{donator_email}", donatorEmail)
                    .replace("{donator_is_user}", String.valueOf(isDonatorUser))
                    .replace("{message}", messagex);

            Component finalMessage = ColorParser.of(parsed).parseLegacy().build();
            audiences.all().sendMessage(finalMessage);

        } catch (Exception e) {
            getLogger().severe("Error in handleMessage:");
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        instance = this;
        this.audiences = BukkitAudiences.create(this);
        saveDefaultConfig();
        commandtriggermanager = new donationtriggermanager(this);
        commandtriggermanager.loadFromDonationTriggerConfig();
        try {
            setupFlowPoll(false, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new commands(this);
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;

        loggx(prefix + " &aSuccessfully enabled! &f(took " + timeTaken + " ms)");
    }

    private void setupFlowPoll(Boolean isreload, CommandSender reloader) throws IOException {
        token = getConfig().getString("token", null);
        prtoken = getConfig().getString("webhook-token", null);
        provider = getConfig().getInt("provider", 0);
        prefix = getConfig().getString("prefix", "&9[flowsense]");

        consolelog = getConfig().getBoolean("log-donation", false);
        isMessage = getConfig().getBoolean("broadcast-message", true);
        isCommandTrigger = getConfig().getBoolean("command-trigger", true);

        if (prtoken == null && token == null) {
            loggx(prefix + " &cConfig error: Token and Webhook-Token is missing! &econfigure it in config.yml");
        } else if (prtoken == null || prtoken.isEmpty()) {
            loggx(prefix + " &cConfig error: Webhook-Token is missing! &econfigure it in config.yml");
        } else if (token == null || token.isEmpty()) {
            loggx(prefix + " &cConfig error: Token is missing! &econfigure it in config.yml");
        } else if (provider < 1 || provider > 3) {
            loggx(prefix + " &cConfig error: Provider is not valid! &ereconfigure it in config.yml");
        }

        flowAuth = new FlowAuth();
        clientid = flowAuth.auth(token, provider, prtoken);

        if (clientid == null || clientid.isEmpty()) {
            loggx(  prefix+ " &cAuthentication failed! token is invalid! &ereconfigure it in config.yml");
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            loggx(prefix + " &aAuthentication success! &fwith cliendid &a" + clientid);

        }
    }



    @Override
    public void onDisable() {
        if (audiences != null) audiences.close();
    }

    public static flowsense getInstance() {
        return instance;
    }
}
