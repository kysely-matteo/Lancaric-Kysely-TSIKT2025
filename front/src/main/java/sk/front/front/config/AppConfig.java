package sk.front.front.config;

public class AppConfig {
    public static final String API_BASE_URL = "http://localhost:8080/api";

    public static String getApiUrl(String endpoint) {
        return API_BASE_URL + endpoint;
    }
}
