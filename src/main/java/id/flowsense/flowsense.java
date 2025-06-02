package id.flowsense;


import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import io.github.milkdrinkers.colorparser.ColorParser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;


public class flowsense extends JavaPlugin {

    private static BukkitAudiences audiences;
    private static flowsense instance;
    private static FlowAuth flowAuth;
    donationtriggermanager commandtriggermanager;
    private static boolean polling = false;

    public static String prefix, token, clientid, prtoken;
    public static int provider;
    public static boolean isCommandTrigger, isMessage;


    public static void loggx(String s) {
        Component msg = ColorParser.of(s).parseLegacy().build();
        audiences.console().sendMessage(msg);
    }

    public static void sendColored(CommandSender sender, String message) {
        Component msg = ColorParser.of(message).parseLegacy().build();
        audiences.sender(sender).sendMessage(msg);
    }

    public void handleMessage(JsonObject entry) {
        try {
//            Getting the required value
            String createdAt = entry.getString("created_at");
            int idx = entry.getInt("id");
            int amountRaw = entry.getInt("amount");
            String donatorName = entry.getString("donator_name");
            String donatorEmail = entry.getString("donator_email");
            String messagex = entry.getString("message");
            String unitx = entry.getString("unit");
            int unitqty = entry.getInt("unit");
            String amountFormatted_US = NumberFormat.getNumberInstance(Locale.US).format(amountRaw);
            String amountFormatted_DE = NumberFormat.getNumberInstance(Locale.GERMAN).format(amountRaw);

//            Making "Provider Name"
            int providerId = entry.getInt("provider");
            String providerName = "";
            switch (providerId) {
                case 1:
                    providerName = "Saweria";
                    break;
                case 2:
                    providerName = "Tako";
                    break;
                case 3:
                    providerName = "Trakteer";
                    break;
            }

            String messageTemplate = getConfig().getString("message", "");
            String parsed = messageTemplate
                    .replace("{id}", String.valueOf(idx))
                    .replace("{created_at}", createdAt)
                    .replace("{provider_id}", String.valueOf(providerId))
                    .replace("{provider_name}", providerName)
                    .replace("{amount_raw}", String.valueOf(amountRaw))
                    .replace("{amount_formatted}", amountFormatted_US)
                    .replace("{amount_formatted_US}", amountFormatted_US)
                    .replace("{amount_formatted_DE}", amountFormatted_DE)
                    .replace("{donator_name}", donatorName)
                    .replace("{donator_email}", donatorEmail)
                    .replace("{message}", messagex)
                    .replace("{unit}", unitx)
                    .replace("{unit_qty}", String.valueOf(unitqty));

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
        boolean setup = true;
        try {
            setup = setupFlowPoll(false, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new commands(this);
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;

        if (setup) {
            loggx(prefix + " &aSuccessfully enabled! &f(took " + timeTaken + " ms)");
        } else {
            loggx(prefix + " &cPlugin not enabled! &f(took " + timeTaken + " ms)");
        }

    }



    public void restartFlowPoll(CommandSender sender) {
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    private boolean setupFlowPoll(Boolean isreload, CommandSender reloader) throws IOException {
        token = getConfig().getString("token", null);
        prtoken = getConfig().getString("webhook-token", null);
        provider = getConfig().getInt("provider", 0);
        prefix = getConfig().getString("prefix", "&9[flowsense]");

        isMessage = getConfig().getBoolean("broadcast-message", true);
        isCommandTrigger = getConfig().getBoolean("command-trigger", true);


        if (isNullOrEmpty(prtoken) && isNullOrEmpty(token)) {
            loggx(prefix + " &cConfig error: Both &e'token' &cand &e'webhook-token' &care missing! &7Please set them in config.yml.");
            return false;
        } else if (isNullOrEmpty(prtoken)) {
            loggx(prefix + " &cConfig error: Missing &e'webhook-token'&c in config.yml.");
            return false;
        } else if (isNullOrEmpty(token)) {
            loggx(prefix + " &cConfig error: Missing &e'token'&c in config.yml.");
            return false;
        } else if (provider < 1 || provider > 3) {
            loggx(prefix + " &cConfig error: &e'provider' &cvalue is invalid! &7Accepted values: 1, 2, or 3.");
            return false;
        }



        flowAuth = new FlowAuth();
        clientid = flowAuth.auth(token, provider, prtoken);

        if (clientid == null || clientid.isEmpty()) {
            loggx(  prefix+ " &cAuthentication failed! token is invalid! ");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        } else {
            loggx(prefix + " &aAuthentication success! &fwith cliendid &a" + clientid);
           pollingthread.start();
            return true;
        }
    }

    Thread pollingthread = new Thread(() -> {
//        loggx(prefix + " &eThread Running! &7Token " + token + " | &7Id " + clientid);
        polling = true;
        while (polling) {
            try {
                boolean updated = FlowAuth.update(token, clientid);
                JsonObject entry = FlowPoll.get(token, clientid);
                if (entry != null) {
                    loggx(JsonWriter.string(entry));
                }
            } catch (IOException e) {
                loggx(prefix + " &cPolling Error!");
                e.printStackTrace();
                break;
            }
            try {
                Thread.sleep(33);
            } catch (InterruptedException e) {
                break;
            }
        }
    });



    @Override
    public void onDisable() {
        polling = false;
        try {
            FlowAuth.exit(token, clientid);
        } catch (IOException e) {
            loggx(prefix + " &eError when stopping client!" + e.toString());
        }
        if (audiences != null) audiences.close();
    }

    public static flowsense getInstance() {
        return instance;
    }
}
