package id.sattr;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class event extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String rawDonationData;
    private final JsonObject parsedData;

    public event(String rawDonationData) {
        this.rawDonationData = rawDonationData;
        this.parsedData = JsonParser.parseString(rawDonationData).getAsJsonObject();
    }

    public String getRawDonationData() {
        return rawDonationData;
    }
    public String getId() {
        return parsedData.get("id").getAsString();
    }
    public String getVersion() {
        return parsedData.get("version").getAsString();
    }
    public String getCreatedAt() {
        return parsedData.get("created_at").getAsString();
    }
    public String getType() {
        return parsedData.get("type").getAsString();
    }
    public int getAmountRaw() {
        return parsedData.get("amount_raw").getAsInt();
    }

    public int getCut() {
        return parsedData.get("cut").getAsInt();
    }

    public String getDonatorName() {
        return parsedData.get("donator_name").getAsString();
    }

    public String getDonatorEmail() {
        return parsedData.get("donator_email").getAsString();
    }

    public boolean isDonatorUser() {
        return parsedData.get("donator_is_user").getAsBoolean();
    }

    public String getMessage() {
        return parsedData.get("message").getAsString();
    }


    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
