package sk.semestralka.studybase.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("service", "Study Platform API");
        status.put("version", "1.0.0");
        return status;
    }
    @GetMapping("/info")
    public Map<String, String> info() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Collaborative Study Platform");
        info.put("description", "Semestrálna práca - Študijná platforma");
        info.put("technology", "Spring Boot 3.5.7 + Java 25 + SQLite");
        return info;
    }

}
