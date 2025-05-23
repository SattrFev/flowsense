package id.flowsense;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FlowPoll {

    private static final String POLL_URL = "https://ux.appcloud.id/catcher/oentry.php";

    public static JsonObject get(String token, String clientId) {
        try {

            String requestBody = JsonWriter.string()
                    .object()
                    .value("token", token)
                    .value("clientid", Integer.parseInt(clientId))
                    .end()
                    .done();

            URL url = new URL(POLL_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes());
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            String jsonString = response.toString().trim();
            if (jsonString.equals("null") || jsonString.isEmpty()) {
                return null;
            }

            return JsonParser.object().from(jsonString);

        } catch (IOException e) {
            System.err.println("Error during poll: " + e.getMessage());
            return null;
        } catch (JsonParserException e) {
            throw new RuntimeException(e);
        }
    }
}
