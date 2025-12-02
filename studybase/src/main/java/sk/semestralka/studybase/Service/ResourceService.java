package sk.semestralka.studybase.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.semestralka.studybase.DTO.CreateResourceRequest;
import sk.semestralka.studybase.DTO.NotificationMessage;
import sk.semestralka.studybase.DTO.ResourceResponse;
import sk.semestralka.studybase.Entity.Resource;
import sk.semestralka.studybase.Entity.User;
import sk.semestralka.studybase.Repository.ResourceRepository;
import sk.semestralka.studybase.Repository.UserRepository;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired  // Pridaj tento dependency pre notifikácie
    private NotificationService notificationService;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResourceResponse createResource(Long groupId, CreateResourceRequest request, Long userId) {
        Resource resource = new Resource();
        resource.setGroupId(groupId);
        resource.setUploadedBy(userId);
        resource.setTitle(request.getTitle());
        resource.setType(request.getType());
        resource.setPathOrUrl(request.getPathOrUrl());

        Resource savedResource = resourceRepository.save(resource);

        // Získaj používateľa pre notifikáciu
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vytvor a odošli notifikáciu
        NotificationMessage notification = new NotificationMessage();
        notification.setType("RESOURCE_CREATED");
        notification.setTitle("Nový materiál");
        notification.setMessage("Pridaný nový materiál: " + savedResource.getTitle());
        notification.setGroupId(groupId);
        notification.setUserId(userId);
        notification.setUserName(user.getName());
        notification.setTimestamp(LocalDateTime.now().format(formatter));

        notificationService.sendNotificationToGroup(groupId, notification);

        return convertToResponse(savedResource);
    }

    public List<ResourceResponse> getGroupResources(Long groupId) {
        List<Resource> resources = resourceRepository.findByGroupId(groupId);
        return resources.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public void deleteResource(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        // Získaj používateľa pre notifikáciu
        User user = userRepository.findById(resource.getUploadedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Vytvor notifikáciu PRED vymazaním (aby sme mali dáta)
        NotificationMessage notification = new NotificationMessage();
        notification.setType("RESOURCE_DELETED");
        notification.setTitle("Materiál odstránený");
        notification.setMessage("Odstránený materiál: " + resource.getTitle());
        notification.setGroupId(resource.getGroupId());
        notification.setUserId(resource.getUploadedBy());
        notification.setUserName(user.getName());
        notification.setTimestamp(LocalDateTime.now().format(formatter));

        // Ak je to FILE, zmazať aj fyzický súbor
        if ("FILE".equals(resource.getType())) {
            try {
                File file = fileStorageService.getFile(resource.getPathOrUrl());
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                // Log chybu, ale pokračuj v mazaní z DB
                System.err.println("Failed to delete physical file: " + e.getMessage());
            }
        }

        resourceRepository.delete(resource);

        // Odošli notifikáciu PO vymazaní z DB (aby sme nestratili dáta)
        notificationService.sendNotificationToGroup(resource.getGroupId(), notification);
    }

    public File getFileForDownload(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!"FILE".equals(resource.getType())) {
            throw new RuntimeException("Resource is not a file");
        }

        return fileStorageService.getFile(resource.getPathOrUrl());
    }

    private ResourceResponse convertToResponse(Resource resource) {
        ResourceResponse response = new ResourceResponse();
        response.setResourceId(resource.getResourceId());
        response.setGroupId(resource.getGroupId());
        response.setUploadedBy(resource.getUploadedBy());
        response.setTitle(resource.getTitle());
        response.setType(resource.getType());
        response.setPathOrUrl(resource.getPathOrUrl());
        response.setUploadedAt(resource.getUploadedAt());

        // Získaj meno uploadera z User entity
        String uploaderName = getUserName(resource.getUploadedBy());
        response.setUploaderName(uploaderName);

        return response;
    }

    private String getUserName(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
            return user.getName();
        } catch (Exception e) {
            return "Unknown User";
        }
    }
}