package id.flowsense;

import java.io.*;
import java.net.*;

public class FlowAuth {
    private static final String AUTH_URL = "https://ux.appcloud.id/auth/auth.php";
    private static final String UPDATE_URL = "https://ux.appcloud.id/auth/update.php";
    private static final String EXIT_URL = "https://ux.appcloud.id/auth/exit.php";

    private static String extractClientId(String json) {
        String key = "\"client_id\":\"";
        int start = json.indexOf(key);
        if (start == -1) return null;
        start += key.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private static String sendPostRequest(String urlStr, String urlParameters) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(urlParameters.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    public static String auth(String token, int provider, String prtoken) throws IOException {
        String params = "token=" + URLEncoder.encode(token, "UTF-8") +
                "&provider=" + provider +
                "&prtoken=" + URLEncoder.encode(prtoken, "UTF-8");
        return extractClientId(sendPostRequest(AUTH_URL, params));
    }

    public static Boolean update(String token, String clientId) throws IOException {
        String params = "token=" + URLEncoder.encode(token, "UTF-8") +
                "&clientid=" + URLEncoder.encode(clientId, "UTF-8");
        String response = sendPostRequest(UPDATE_URL, params);
        return response.trim().equalsIgnoreCase("true");
    }

    public static Boolean exit(String token, String clientId) throws IOException {
        String params = "token=" + URLEncoder.encode(token, "UTF-8") +
                "&clientid=" + URLEncoder.encode(clientId, "UTF-8");
        String response = sendPostRequest(EXIT_URL, params);
        return response.trim().equalsIgnoreCase("true");
    }
}
