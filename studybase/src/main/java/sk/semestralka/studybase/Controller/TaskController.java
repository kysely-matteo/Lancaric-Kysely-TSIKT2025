package sk.semestralka.studybase.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.semestralka.studybase.DTO.CreateTaskRequest;
import sk.semestralka.studybase.DTO.TaskResponse;
import sk.semestralka.studybase.DTO.UpdateTaskRequest;
import sk.semestralka.studybase.Service.TaskService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups/{groupId}/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * Vytvorenie novej úlohy v skupine
     * POST http://localhost:8080/api/groups/1/tasks?userId=1
     */
    @PostMapping
    public ResponseEntity<?> createTask(@PathVariable Long groupId,
                                        @RequestBody CreateTaskRequest request,
                                        @RequestParam Long userId) {
        try {
            TaskResponse taskResponse = taskService.createTask(groupId, request, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Úloha bola úspešne vytvorená");
            response.put("task", taskResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Získanie všetkých úloh skupiny
     * GET http://localhost:8080/api/groups/1/tasks
     */
    @GetMapping
    public ResponseEntity<?> getGroupTasks(@PathVariable Long groupId) {
        try {
            List<TaskResponse> tasks = taskService.getGroupTasks(groupId);
            return ResponseEntity.ok(tasks);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Získanie úloh skupiny podľa stavu
     * GET http://localhost:8080/api/groups/1/tasks/status/OPEN
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getGroupTasksByStatus(@PathVariable Long groupId,
                                                   @PathVariable String status) {
        try {
            List<TaskResponse> tasks = taskService.getGroupTasksByStatus(groupId, status);
            return ResponseEntity.ok(tasks);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Aktualizácia úlohy
     * PUT http://localhost:8080/api/groups/1/tasks/1
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable Long groupId,
                                        @PathVariable Long taskId,
                                        @RequestBody UpdateTaskRequest request) {
        try {
            TaskResponse taskResponse = taskService.updateTask(taskId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Úloha bola úspešne aktualizovaná");
            response.put("task", taskResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Zmena stavu úlohy
     * PATCH http://localhost:8080/api/groups/1/tasks/1/status?status=IN_PROGRESS
     */
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Long groupId,
                                              @PathVariable Long taskId,
                                              @RequestParam String status) {
        try {
            TaskResponse taskResponse = taskService.updateTaskStatus(taskId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stav úlohy bol úspešne zmenený");
            response.put("task", taskResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Odstránenie úlohy
     * DELETE http://localhost:8080/api/groups/1/tasks/1
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Long groupId,
                                        @PathVariable Long taskId) {
        try {
            taskService.deleteTask(taskId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Úloha bola úspešne odstránená");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
