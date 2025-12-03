package sk.semestralka.studybase.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.semestralka.studybase.DTO.CreateTaskRequest;
import sk.semestralka.studybase.DTO.NotificationMessage;
import sk.semestralka.studybase.DTO.TaskResponse;
import sk.semestralka.studybase.DTO.UpdateTaskRequest;
import sk.semestralka.studybase.Entity.Task;
import sk.semestralka.studybase.Entity.User;
import sk.semestralka.studybase.Repository.TaskRepository;
import sk.semestralka.studybase.Repository.UserRepository;
import sk.semestralka.studybase.Repository.MembershipRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<TaskResponse> getGroupTasks(Long groupId) {
        List<Task> tasks = taskRepository.findByGroupId(groupId);
        return tasks.stream()
                .map(task -> {
                    String creatorName = userRepository.findById(task.getCreatedBy())
                            .map(User::getName)
                            .orElse("Neznámy používateľ");
                    return convertToTaskResponse(task, creatorName);
                })
                .collect(Collectors.toList());
    }

    public TaskResponse createTask(Long groupId, CreateTaskRequest request, Long creatorUserId) {
        if (!membershipRepository.existsByUserIdAndGroupId(creatorUserId, groupId)) {
            throw new RuntimeException("Používateľ nie je členom skupiny alebo skupina neexistuje");
        }

        Task task = new Task();
        task.setGroupId(groupId);
        task.setCreatedBy(creatorUserId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus("OPEN");
        task.setDeadline(request.getDeadline());

        Task savedTask = taskRepository.save(task);

        User creatorUser = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("Používateľ nebol nájdený"));
        String creatorName = creatorUser.getName();

        // Vytvorenie notifikácie
        NotificationMessage notification = new NotificationMessage(
                "TASK_CREATED",
                "Nová úloha",
                "Bola vytvorená nová úloha: '" + savedTask.getTitle() + "' v skupine",
                groupId,
                creatorUserId,
                creatorName,
                LocalDateTime.now().format(formatter)
        );

        notificationService.sendNotificationToGroup(groupId, notification);

        return convertToTaskResponse(savedTask, creatorName);
    }

    public List<TaskResponse> getGroupTasksByStatus(Long groupId, String status) {
        List<Task> tasks = taskRepository.findByGroupIdAndStatus(groupId, status);
        return tasks.stream()
                .map(task -> {
                    String creatorName = userRepository.findById(task.getCreatedBy())
                            .map(User::getName)
                            .orElse("Neznámy používateľ");
                    return convertToTaskResponse(task, creatorName);
                })
                .collect(Collectors.toList());
    }

    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("Úloha nebola nájdená: " + taskId);
        }

        Task task = taskOpt.get();
        String oldTitle = task.getTitle();

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null && !request.getStatus().equals(task.getStatus())) {
            // Notify about status change
            String oldStatus = task.getStatus();
            task.setStatus(request.getStatus());

            User updater = userRepository.findById(task.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("Používateľ nebol nájdený"));

            NotificationMessage statusNotification = new NotificationMessage(
                    "TASK_STATUS_CHANGED",
                    "Zmena stavu úlohy",
                    "Úloha '" + task.getTitle() + "' zmenená z '" + oldStatus + "' na '" + request.getStatus() + "'",
                    task.getGroupId(),
                    task.getCreatedBy(),
                    updater.getName(),
                    LocalDateTime.now().format(formatter)
            );
            notificationService.sendNotificationToGroup(task.getGroupId(), statusNotification);
        }

        Task updatedTask = taskRepository.save(task);

        // Send update notification if title changed
        if (request.getTitle() != null && !oldTitle.equals(request.getTitle())) {
            User updater = userRepository.findById(task.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("Používateľ nebol nájdený"));

            NotificationMessage updateNotification = new NotificationMessage(
                    "TASK_UPDATED",
                    "Úloha aktualizovaná",
                    "Úloha '" + oldTitle + "' bola premenovaná na '" + request.getTitle() + "'",
                    task.getGroupId(),
                    task.getCreatedBy(),
                    updater.getName(),
                    LocalDateTime.now().format(formatter)
            );
            notificationService.sendNotificationToGroup(task.getGroupId(), updateNotification);
        }

        String creatorName = userRepository.findById(updatedTask.getCreatedBy())
                .map(User::getName)
                .orElse("Neznámy používateľ");

        return convertToTaskResponse(updatedTask, creatorName);
    }

    public TaskResponse updateTaskStatus(Long taskId, String status) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("Úloha nebola nájdená: " + taskId);
        }

        Task task = taskOpt.get();
        String oldStatus = task.getStatus();
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        String creatorName = userRepository.findById(updatedTask.getCreatedBy())
                .map(User::getName)
                .orElse("Neznámy používateľ");

        // Notify about status change
        NotificationMessage notification = new NotificationMessage(
                "TASK_STATUS_CHANGED",
                "Zmena stavu úlohy",
                "Úloha '" + task.getTitle() + "' zmenená z '" + oldStatus + "' na '" + status + "'",
                task.getGroupId(),
                task.getCreatedBy(),
                creatorName,
                LocalDateTime.now().format(formatter)
        );
        notificationService.sendNotificationToGroup(task.getGroupId(), notification);

        return convertToTaskResponse(updatedTask, creatorName);
    }

    public void deleteTask(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            throw new RuntimeException("Úloha nebola nájdená: " + taskId);
        }

        Task task = taskOpt.get();
        String creatorName = userRepository.findById(task.getCreatedBy())
                .map(User::getName)
                .orElse("Neznámy používateľ");

        // Send delete notification BEFORE deleting
        NotificationMessage notification = new NotificationMessage(
                "TASK_DELETED",
                "Úloha odstránená",
                "Úloha '" + task.getTitle() + "' bola odstránená",
                task.getGroupId(),
                task.getCreatedBy(),
                creatorName,
                LocalDateTime.now().format(formatter)
        );
        notificationService.sendNotificationToGroup(task.getGroupId(), notification);

        taskRepository.deleteById(taskId);
    }

        private TaskResponse convertToTaskResponse(Task task, String creatorName) {
            return new TaskResponse(
                    task.getTaskId(),
                    task.getGroupId(),
                    task.getCreatedBy(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getDeadline(),
                    task.getCreatedAt(),
                    creatorName
            );
        }
    }