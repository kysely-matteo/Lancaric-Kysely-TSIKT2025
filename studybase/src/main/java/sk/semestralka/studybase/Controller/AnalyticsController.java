package sk.semestralka.studybase.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.semestralka.studybase.DTO.GroupAnalyticsDTO;
import sk.semestralka.studybase.DTO.UserAnalyticsDTO;
import sk.semestralka.studybase.Service.AnalyticsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroupAnalytics(@PathVariable Long groupId) {
        try {
            GroupAnalyticsDTO analytics = analyticsService.getGroupAnalytics(groupId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analytics", analytics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserAnalytics(@PathVariable Long userId) {
        try {
            UserAnalyticsDTO analytics = analyticsService.getUserAnalytics(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("analytics", analytics);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}