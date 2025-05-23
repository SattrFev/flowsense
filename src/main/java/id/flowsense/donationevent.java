package id.flowsense;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class donationevent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String rawDonationData;
    private final JsonObject parsedData;

    public donationevent(String rawDonationData) {
        this.rawDonationData = rawDonationData;
        try {
            this.parsedData = JsonParser.object().from(rawDonationData);
        } catch (Exception e) {
            throw new RuntimeException("failed to parse donation json", e);
        }
    }

    public String getRaw() {
        return rawDonationData;
    }

    public String getId() {
        return (String) parsedData.get("id");
    }

    public String getCreatedAt() {
        return (String) parsedData.get("created_at");
    }

    public int getAmountRaw() {
        return ((Number) parsedData.get("amount_raw")).intValue();
    }

    public String getDonatorName() {
        return (String) parsedData.get("donator_name");
    }

    public String getDonatorEmail() {
        return (String) parsedData.get("donator_email");
    }

    public String getMessage() {
        return (String) parsedData.get("message");
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
