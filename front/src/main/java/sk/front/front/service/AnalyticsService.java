package sk.front.front.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import sk.front.front.config.AppConfig;
import sk.front.front.model.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class AnalyticsService {
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl = AppConfig.getApiUrl("/analytics");

    public GroupAnalyticsDTO getGroupAnalytics(Long groupId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(baseUrl + "/group/" + groupId))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, Object> errorResponse = mapper.readValue(response.body(), Map.class);
            throw new RuntimeException((String) errorResponse.get("message"));
        }

        Map<String, Object> responseBody = mapper.readValue(response.body(), Map.class);
        Map<String, Object> analyticsData = (Map<String, Object>) responseBody.get("analytics");

        return mapper.convertValue(analyticsData, GroupAnalyticsDTO.class);
    }

    public UserAnalyticsDTO getUserAnalytics(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(baseUrl + "/user/" + userId))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, Object> errorResponse = mapper.readValue(response.body(), Map.class);
            throw new RuntimeException((String) errorResponse.get("message"));
        }

        Map<String, Object> responseBody = mapper.readValue(response.body(), Map.class);
        Map<String, Object> analyticsData = (Map<String, Object>) responseBody.get("analytics");

        return mapper.convertValue(analyticsData, UserAnalyticsDTO.class);
    }
}