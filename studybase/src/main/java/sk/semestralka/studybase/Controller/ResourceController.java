package sk.semestralka.studybase.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sk.semestralka.studybase.DTO.CreateResourceRequest;
import sk.semestralka.studybase.DTO.ResourceResponse;
import sk.semestralka.studybase.Service.FileStorageService;
import sk.semestralka.studybase.Service.NotificationService;
import sk.semestralka.studybase.Service.ResourceService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups/{groupId}/resources")
public class ResourceController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<?> createResource(@PathVariable Long groupId,
                                            @RequestBody CreateResourceRequest request,
                                            @RequestParam Long userId) {
        try {
            ResourceResponse resourceResponse = resourceService.createResource(groupId, request, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Materiál bol úspešne pridaný");
            response.put("resource", resourceResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@PathVariable Long groupId,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam String title,
                                        @RequestParam Long userId) {
        try {
            // Ulož súbor
            String fileName = fileStorageService.storeFile(file);

            // Vytvor resource
            CreateResourceRequest request = new CreateResourceRequest();
            request.setTitle(title);
            request.setType("FILE");
            request.setPathOrUrl(fileName);

            ResourceResponse resourceResponse = resourceService.createResource(groupId, request, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Súbor bol úspešne nahraný");
            response.put("resource", resourceResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getGroupResources(@PathVariable Long groupId) {
        try {
            List<ResourceResponse> resources = resourceService.getGroupResources(groupId);
            return ResponseEntity.ok(resources);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Pridaj endpoint pre download súboru - OPRAVENÝ
    @GetMapping("/{resourceId}/download")
    public ResponseEntity<FileSystemResource> downloadFile(@PathVariable Long resourceId) {
        try {
            java.io.File file = resourceService.getFileForDownload(resourceId);

            FileSystemResource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Pridaj DELETE endpoint - POZOR: zmenená URL!
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<?> deleteResource(@PathVariable Long resourceId) {
        try {
            resourceService.deleteResource(resourceId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Materiál bol zmazaný");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}