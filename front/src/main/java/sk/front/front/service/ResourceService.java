package sk.front.front.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import sk.front.front.config.AppConfig;
import sk.front.front.model.Resource;
import sk.front.front.model.User;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceService {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public ResourceService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public List<Resource> getGroupResources(Long groupId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups/" + groupId + "/resources")))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Resource.class));
        } else {
            throw new RuntimeException("Failed to fetch group resources: " + response.body());
        }
    }

    public Resource uploadFileToGroup(Long groupId, File file, String title) throws Exception {
        String url = AppConfig.getApiUrl("/groups/" + groupId + "/resources/upload");

        // Vytvorenie multipart request pomocou HttpURLConnection
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Multipart form-data
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

            // File part
            writer.append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getName()).append("\"\r\n");
            writer.append("Content-Type: ").append(Files.probeContentType(file.toPath())).append("\r\n");
            writer.append("\r\n").flush();

            Files.copy(file.toPath(), outputStream);
            outputStream.flush();

            // Title part
            writer.append("\r\n").append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"title\"\r\n");
            writer.append("\r\n").append(title).append("\r\n").flush();

            // User ID part
            writer.append("\r\n").append("--").append(boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"userId\"\r\n");
            writer.append("\r\n").append(String.valueOf(getCurrentUserId())).append("\r\n").flush();

            // End
            writer.append("\r\n").append("--").append(boundary).append("--").append("\r\n").flush();
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Spracovanie odpovede
            try (InputStream inputStream = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                Map<String, Object> responseMap = mapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {});
                if (Boolean.TRUE.equals(responseMap.get("success"))) {
                    Map<String, Object> resourceMap = (Map<String, Object>) responseMap.get("resource");
                    return mapper.convertValue(resourceMap, Resource.class);
                } else {
                    throw new RuntimeException((String) responseMap.get("message"));
                }
            }
        } else {
            throw new RuntimeException("Upload failed with HTTP code: " + responseCode);
        }
    }

    // Metóda pre vytvorenie linku
    public Resource createResourceLink(Long groupId, String title, String url) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("title", title);
        requestBody.put("type", "LINK");
        requestBody.put("pathOrUrl", url);

        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups/" + groupId + "/resources?userId=" + getCurrentUserId())))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        Map<String, Object> responseMap = mapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

        if (response.statusCode() == 200 && Boolean.TRUE.equals(responseMap.get("success"))) {
            Map<String, Object> resourceMap = (Map<String, Object>) responseMap.get("resource");
            return mapper.convertValue(resourceMap, Resource.class);
        } else {
            throw new RuntimeException((String) responseMap.get("message"));
        }
    }

    public void deleteResource(Long groupId, Long resourceId) throws Exception {



        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups/" + groupId + "/resources/" + resourceId)))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, Object> errorResponse = mapper.readValue(response.body(), Map.class);
            throw new RuntimeException((String) errorResponse.get("message"));
        }
    }

    // Pomocná metóda pre získanie aktuálneho userId
    private Long getCurrentUserId() {
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUserId();
        }
        throw new RuntimeException("User not logged in");
    }
}