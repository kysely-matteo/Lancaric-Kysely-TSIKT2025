package sk.front.front.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import sk.front.front.config.AppConfig;
import sk.front.front.model.*;


import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

public class ApiService {
    private final HttpClient client;
    private final ObjectMapper mapper;

    public ApiService() {
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public User login(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(email, password);
        String jsonBody = mapper.writeValueAsString(loginRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/auth/login")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseMap = mapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});

        if (response.statusCode() == 200 && Boolean.TRUE.equals(responseMap.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) responseMap.get("user");

            User user = new User();
            user.setUserId(Long.valueOf(userMap.get("userId").toString()));
            user.setName((String) userMap.get("name"));
            user.setEmail((String) userMap.get("email"));

            if (userMap.get("createdAt") != null) {
                String createdAtStr = userMap.get("createdAt").toString();
                user.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
            }

            return user;
        } else {
            throw new RuntimeException((String) responseMap.get("message"));
        }
    }

    public User register(String name, String email, String password) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(name, email, password);
        String jsonBody = mapper.writeValueAsString(registerRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/auth/register")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Map<String, Object> responseMap = mapper.readValue(response.body(),
                new TypeReference<Map<String, Object>>() {});

        if (response.statusCode() == 200 && Boolean.TRUE.equals(responseMap.get("success"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) responseMap.get("user");

            User user = new User();
            user.setUserId(Long.valueOf(userMap.get("userId").toString()));
            user.setName((String) userMap.get("name"));
            user.setEmail((String) userMap.get("email"));

            if (userMap.get("createdAt") != null) {
                String createdAtStr = userMap.get("createdAt").toString();
                user.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
            }

            return user;
        } else {
            throw new RuntimeException((String) responseMap.get("message"));
        }
    }



    public List<GroupResponse> getUserGroups(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups/user/" + userId)))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, GroupResponse.class));
        } else {
            throw new RuntimeException("Failed to fetch user groups: " + response.body());
        }
    }

    public GroupResponse createGroup(String name, String description, Long userId) throws Exception {
        CreateGroupRequest createRequest = new CreateGroupRequest(name, description);
        String jsonBody = mapper.writeValueAsString(createRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups?userId=" + userId)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        Map responseMap = mapper.readValue(responseBody, Map.class);

        if (response.statusCode() == 200 && Boolean.TRUE.equals(responseMap.get("success"))) {
            Map<String, Object> groupMap = (Map<String, Object>) responseMap.get("group");

            GroupResponse group = new GroupResponse();
            group.setGroupId(Long.valueOf(groupMap.get("groupId").toString()));
            group.setName((String) groupMap.get("name"));
            group.setDescription((String) groupMap.get("description"));
            group.setCreatedBy(Long.valueOf(groupMap.get("createdBy").toString()));
            group.setMemberCount((Integer) groupMap.get("memberCount"));

            return group;
        } else {
            throw new RuntimeException((String) responseMap.get("message"));
        }
    }

    public List<GroupResponse> getAllGroups() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups")))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, GroupResponse.class));
        } else {
            throw new RuntimeException("Failed to fetch all groups: " + response.body());
        }
    }

    public UserResponse getUserById(Long userId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/auth/user/" + userId)))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), UserResponse.class);
        } else {
            Map<String, Object> errorResponse = mapper.readValue(response.body(), Map.class);
            throw new RuntimeException((String) errorResponse.get("message"));
        }
    }

    public MembershipResponse addMemberToGroup(Long groupId, Long userId, String role) throws Exception {
        String url = AppConfig.getApiUrl("/groups/" + groupId + "/members?userId=" + userId + "&role=" + role);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        Map responseMap = mapper.readValue(responseBody, Map.class);

        if (response.statusCode() == 200 && Boolean.TRUE.equals(responseMap.get("success"))) {
            Map<String, Object> membershipMap = (Map<String, Object>) responseMap.get("membership");

            MembershipResponse membership = new MembershipResponse();
            membership.setMembershipId(Long.valueOf(membershipMap.get("membershipId").toString()));
            membership.setUserId(Long.valueOf(membershipMap.get("userId").toString()));
            membership.setGroupId(Long.valueOf(membershipMap.get("groupId").toString()));
            membership.setRole((String) membershipMap.get("role"));
            membership.setUserName((String) membershipMap.get("userName"));

            return membership;
        } else {
            throw new RuntimeException((String) responseMap.get("message"));
        }
    }

    public List<MembershipResponse> getGroupMembers(Long groupId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups/" + groupId + "/members")))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, MembershipResponse.class));
        } else {
            throw new RuntimeException("Failed to fetch group members: " + response.body());
        }
    }

    public Task createTask(Long groupId, String title, String description, LocalDateTime deadline, Long userId) throws Exception {
        CreateTaskRequest createRequest = new CreateTaskRequest(title, description, deadline);
        String jsonBody = mapper.writeValueAsString(createRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups/" + groupId + "/tasks?userId=" + userId)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        Map responseMap = mapper.readValue(responseBody, Map.class);

        if (response.statusCode() == 200 && Boolean.TRUE.equals(responseMap.get("success"))) {
            Map<String, Object> taskMap = (Map<String, Object>) responseMap.get("task");

            Task task = new Task();
            task.setTaskId(Long.valueOf(taskMap.get("taskId").toString()));
            task.setGroupId(Long.valueOf(taskMap.get("groupId").toString()));
            task.setCreatedBy(Long.valueOf(taskMap.get("createdBy").toString()));
            task.setTitle((String) taskMap.get("title"));
            task.setDescription((String) taskMap.get("description"));
            task.setStatus((String) taskMap.get("status"));
            task.setCreatorName((String) taskMap.get("creatorName"));

            if (taskMap.get("deadline") != null) {
                String deadlineStr = taskMap.get("deadline").toString();
                task.setDeadline(LocalDateTime.parse(deadlineStr.replace(" ", "T")));
            }

            if (taskMap.get("createdAt") != null) {
                String createdAtStr = taskMap.get("createdAt").toString();
                task.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
            }

            return task;
        } else {
            throw new RuntimeException((String) responseMap.get("message"));
        }
    }

    public List<Task> getGroupTasks(Long groupId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups/" + groupId + "/tasks")))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(),
                    mapper.getTypeFactory().constructCollectionType(List.class, Task.class));
        } else {
            throw new RuntimeException("Failed to fetch group tasks: " + response.body());
        }
    }

    public Task updateTaskStatus(Long groupId, Long taskId, String status) throws Exception {
        String url = AppConfig.getApiUrl("/groups/" + groupId + "/tasks/" + taskId + "/status?status=" + status);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.noBody()) // Workaround pre PATCH
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();
        Map responseMap = mapper.readValue(responseBody, Map.class);

        if (response.statusCode() == 200 && Boolean.TRUE.equals(responseMap.get("success"))) {
            Map<String, Object> taskMap = (Map<String, Object>) responseMap.get("task");

            Task task = new Task();
            task.setTaskId(Long.valueOf(taskMap.get("taskId").toString()));
            task.setTitle((String) taskMap.get("title"));
            task.setStatus((String) taskMap.get("status"));

            return task;
        } else {
            throw new RuntimeException((String) responseMap.get("message"));
        }
    }

    public void deleteTask(Long groupId, Long taskId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(AppConfig.getApiUrl("/groups/" + groupId + "/tasks/" + taskId)))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Map<String, Object> errorResponse = mapper.readValue(response.body(), Map.class);
            throw new RuntimeException((String) errorResponse.get("message"));
        }
    }
}