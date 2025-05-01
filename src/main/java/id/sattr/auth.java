package id.sattr;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static id.sattr.flowsense.*;

public class auth {
    private static final String SERVER_URL = "https://ux.appcloud.id/witheverythinguare";
    private static final Gson gson = new Gson();

    public static boolean sendPing(String status, Boolean debug) {
        try {
            URL url = new URL(SERVER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            String countryCode = getCountryFromIP(getPublicIP());

            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("clientid", clientid);
            jsonMap.put("status", status);
            jsonMap.put("nameid", "halcyon01-42925");
            jsonMap.put("country", countryCode);
            jsonMap.put("clientport", portNumber);
            jsonMap.put("endpoint", clientEndpoint);
            jsonMap.put("streamkey", streamkey);

            String jsonInputString = gson.toJson(jsonMap);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // baca response dari server
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                boolean verified = jsonResponse.has("verified") && jsonResponse.get("verified").getAsBoolean();
                String reason = jsonResponse.has("reason") ? jsonResponse.get("reason").getAsString() : "unknown";

                if (debug) {
                    if (verified) {
                        System.out.println("[+] server responded: verified (" + status + ") " + reason);
                    } else {
                        System.out.println("[-] server responded: failed to connect " + reason);
                    }
                }
                return verified;
            }

        } catch (Exception e) {
            if (debug) {
                System.out.println("[x] connection error (" + status + "): " + e.getMessage());
            }
            return false;
        }
    }

    private static String getPublicIP() {
        try {
            URL url = new URL("https://api.ipify.org");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            return in.readLine();
        } catch (Exception e) {
            System.out.println("[x] gagal ambil IP public: " + e.getMessage());
            return "Unknown";
        }
    }

    private static String getCountryFromIP(String ip) {
        try {
            URL url = new URL("http://ip-api.com/json/" + ip + "?fields=countryCode");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
            return jsonResponse.has("countryCode") ? jsonResponse.get("countryCode").getAsString() : "Unknown";

        } catch (Exception e) {
            System.out.println("[x] gagal ambil country code: " + e.getMessage());
            return "Unknown";
        }
    }
}
