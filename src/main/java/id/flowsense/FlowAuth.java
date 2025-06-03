package id.flowsense;

import java.io.*;
import java.net.*;

public class FlowAuth {
    private static final String AUTH_URL = "https://ux.appcloud.id/auth/auth.php";
    private static final String UPDATE_URL = "https://ux.appcloud.id/auth/update.php";
    private static final String EXIT_URL = "https://ux.appcloud.id/auth/exit.php";

    public static class HttpResponse {
        public int responseCode;
        public String body;

        public HttpResponse(int code, String body) {
            this.responseCode = code;
            this.body = body;
        }
    }

    private static HttpResponse sendPostRequest(String urlStr, String urlParameters) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(urlParameters.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 400)
                ? conn.getInputStream()
                : conn.getErrorStream(); // ambil error stream kalau bukan 2xx

        StringBuilder response = new StringBuilder();
        if (inputStream != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
        }

        return new HttpResponse(responseCode, response.toString());
    }

    private static String extractClientId(String json) {
        String key = "\"client_id\":\"";
        int start = json.indexOf(key);
        if (start == -1) return null;
        start += key.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    public static String auth(String token, int provider, String prtoken) throws IOException {
        String params = "token=" + URLEncoder.encode(token, "UTF-8") +
                "&provider=" + provider +
                "&prtoken=" + URLEncoder.encode(prtoken, "UTF-8");
        HttpResponse res = sendPostRequest(AUTH_URL, params);

        if (res.responseCode == 200) {
            return extractClientId(res.body);
        } else {
            return "err:" + res.responseCode;
        }
    }

    public static Boolean update(String token, String clientId) throws IOException {
        String params = "token=" + URLEncoder.encode(token, "UTF-8") +
                "&clientid=" + URLEncoder.encode(clientId, "UTF-8");
        HttpResponse res = sendPostRequest(UPDATE_URL, params);
        return res.responseCode == 200 && res.body.trim().equalsIgnoreCase("true");
    }

    public static Boolean exit(String token, String clientId) throws IOException {
        String params = "token=" + URLEncoder.encode(token, "UTF-8") +
                "&clientid=" + URLEncoder.encode(clientId, "UTF-8");
        HttpResponse res = sendPostRequest(EXIT_URL, params);
        return res.responseCode == 200 && res.body.trim().equalsIgnoreCase("true");
    }
}
