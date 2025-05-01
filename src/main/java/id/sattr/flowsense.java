package id.sattr;

import com.google.gson.*;
import io.github.milkdrinkers.colorparser.ColorParser;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.util.log.Slf4jLog;
import spark.Spark;

import javax.swing.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Timer;
import java.util.TimerTask;

public class flowsense extends JavaPlugin {

    private static BukkitAudiences audiences;
    private static flowsense instance;
    donationtriggermanager commandtriggermanager;

    public static String PREFIXE = "";
    public static String clientid = "";
    public static String streamkey = "";
    public static int portNumber = 3000;
    public static String clientEndpoint = "";
    public static boolean devMode = false;

    public class HashUtil {
        public static String toSHA1(String input) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                byte[] result = md.digest(input.getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : result) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void loggx(String s) {
        Component msg = ColorParser.of(s).parseLegacy().build();
        audiences.console().sendMessage(msg);
    }


    public static void sendColored(CommandSender sender, String message) {
        Component msg = ColorParser.of(message).parseLegacy().build();
        audiences.sender(sender).sendMessage(msg);
    }

    public static String getStreamKeyFromUrl(String url) {
        try {
            int index = url.indexOf("streamKey=");
            if (index == -1) return null;
            String paramPart = url.substring(index + "streamKey=".length());
            int ampIndex = paramPart.indexOf('&');
            if (ampIndex != -1) {
                return paramPart.substring(0, ampIndex);
            }
            return paramPart;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.Slf4jLog");
        instance = this;
        this.audiences = BukkitAudiences.create(this);
        saveDefaultConfig();
        commandtriggermanager = new donationtriggermanager(this);
        commandtriggermanager.loadFromDonationTriggerConfig();
        setupWebhookServer(false, null);
        new commands(this);
        long endTime = System.currentTimeMillis();
        long timeTaken = endTime - startTime;

        loggx(PREFIXE + " &aSuccessfully enabled! &f(took " + timeTaken + " ms)");
    }


    void restartWebhookServer(CommandSender reloader) {
        Spark.stop();
        Bukkit.getScheduler().runTaskLater(this, () -> setupWebhookServer(true, reloader), 20L);
    }

    private void handleMessage(JsonObject objx) {
        try {
            String version = objx.get("version").getAsString();
            String createdAt = objx.get("created_at").getAsString();
            String idx = objx.get("id").getAsString();
            String type = objx.get("type").getAsString();
            int amountRaw = objx.get("amount_raw").getAsInt();
            int cut = objx.get("cut").getAsInt();

            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
            DecimalFormat f = new DecimalFormat("#,###", symbols);
            String amountFormatted = f.format(amountRaw);
            String cutFormatted = f.format(cut);

            String donatorName = objx.get("donator_name").getAsString();
            String donatorEmail = objx.get("donator_email").getAsString();
            String messagex = objx.get("message").getAsString();
            boolean isDonatorUser = objx.get("donator_is_user").getAsBoolean();


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

            Component finalMessage = MiniMessage.miniMessage().deserialize(parsed);
            audiences.all().sendMessage(finalMessage);

        } catch (Exception e) {
            getLogger().severe("Error in handleMessage:");
            e.printStackTrace();
        }
    }

    private void setupWebhookServer(Boolean isreload, CommandSender reloader) {
        clientid = getConfig().getString("token", "");
        streamkey = getStreamKeyFromUrl(getConfig().getString("widget-url", ""));
        portNumber = getConfig().getInt("port", 3000);
        clientEndpoint = getConfig().getString("endpoint-url", "");
        devMode = getConfig().getBoolean("development-mode", false);
        PREFIXE = getConfig().getString("prefix", "&9[flowsense]");

        startHeartbeat();

        boolean consolelog = getConfig().getBoolean("log-donation", true);
        boolean isMessage = getConfig().getBoolean("broadcast-message", true);
        boolean isCommandTrigger = getConfig().getBoolean("command-trigger", true);

        if (streamkey == null && clientid.isEmpty()) {
            if (isreload && reloader != null) {
                if (reloader instanceof Player) {
                    sendColored(reloader, PREFIXE + " &eWidgetURL and Token are &cnull! &eplease configure it in config.yml!");
                    return;
                }
            }
            loggx(PREFIXE + " &eWidgetURL and Token are &cnull! &eplease configure it in config.yml!");
            return;
        }

        if (streamkey == null) {
            if (isreload && reloader != null) {
                if (reloader instanceof Player) {
                    sendColored(reloader,PREFIXE + " &eWidgetURL is &cnull! &eplease configure it in config.yml!");
                    return;
                }
            }
            loggx(PREFIXE + " &eWidgetURL is &cnull! &eplease configure it in config.yml!");
            return;
        }

        if (clientid == null) {
            if (isreload && reloader != null) {
                if (reloader instanceof Player) {
                    sendColored(reloader, PREFIXE + " &eToken is &cnull! &eplease configure it in config.yml!");
                    return;
                }
            }
            loggx(PREFIXE + " &eToken is &cnull! &eplease configure it in config.yml!");
            return;
        }

        if (devMode) {
            if (isreload && reloader != null) {
                if (reloader instanceof Player) {
                    sendColored(reloader, PREFIXE + " &eDevelopment mode is &dactivated!");
                    return;
                }
            }
            loggx(PREFIXE + " &eDevelopment mode is &dactivated!");
            return;
        }

        if (auth.sendPing("on", devMode)) {
            Spark.port(portNumber);
            String path = "/" + HashUtil.toSHA1(clientid);
            Spark.post(path, (req, res) -> {
                String authHeader = req.headers("Authorization");
                if (authHeader == null || !authHeader.equals("Bearer " + clientid)) {
                    res.status(401);
                    return "Unauthorized";
                }

                String requestBody = req.body();
                Bukkit.getScheduler().runTask(this, () -> {
                    Bukkit.getPluginManager().callEvent(new event(requestBody));
                    if (consolelog) loggx(PREFIXE + " &dDonation Recieved!");

                    try {
                        JsonElement parsed = JsonParser.parseString(requestBody);
                        if (!parsed.isJsonObject()) {
                            return;
                        }

                        JsonObject objx = parsed.getAsJsonObject();
                        int amountRaw = objx.get("amount_raw").getAsInt();
                        if (isCommandTrigger) {
                            commandtriggermanager.checkAndRun(amountRaw, objx);
                        };

                        if (isMessage) {
                            handleMessage(objx);
                        }


                    } catch (Exception e) {
                        loggx(PREFIXE + " &cFailed to process incoming webhook request:");
                        e.printStackTrace();
                    }
                });
                res.status(200);
                return "OK";
            });
            if (isreload && reloader != null) {
                if (reloader instanceof Player) {
                    sendColored(reloader, PREFIXE + " plugin enabled on port&a " + portNumber);
                    sendColored(reloader, PREFIXE + " private webhook url&9 " + "https://ux.appcloud.id/to?o=" + HashUtil.toSHA1(clientid));
                    sendColored(reloader, PREFIXE + " &aPlugin Reloaded!");
                }
            }
            loggx(PREFIXE + " &aplugin enabled on port " + portNumber);
            loggx(PREFIXE + " &fprivate webhook url&9 " + "https://ux.appcloud.id/to?o=" + HashUtil.toSHA1(clientid));
            if (isreload) loggx(PREFIXE + " &aPlugin Reloaded!");
        } else {
            loggx(PREFIXE + " &cToken is invalid! plugin stopped");
            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    private static void startHeartbeat() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                auth.sendPing("on", devMode);
            }
        }, 0, 19000);
    }

    @Override
    public void onDisable() {
        if (audiences != null) audiences.close();
    }

    public static flowsense getInstance() {
        return instance;
    }
}
