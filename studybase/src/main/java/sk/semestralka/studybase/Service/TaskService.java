package sk.semestralka.studybase.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sk.semestralka.studybase.DTO.CreateTaskRequest;
import sk.semestralka.studybase.DTO.TaskResponse;
import sk.semestralka.studybase.DTO.UpdateTaskRequest;
import sk.semestralka.studybase.Entity.Task;
import sk.semestralka.studybase.Entity.User;
import sk.semestralka.studybase.Repository.TaskRepository;
import sk.semestralka.studybase.Repository.UserRepository;
import sk.semestralka.studybase.Repository.MembershipRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MembershipRepository membershipRepository;


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


        String creatorName = userRepository.findById(creatorUserId)
                .map(User::getName)
                .orElse("Neznámy používateľ");

        return convertToTaskResponse(savedTask, creatorName);
    }


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


        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }

        Task updatedTask = taskRepository.save(task);

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
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);

        String creatorName = userRepository.findById(updatedTask.getCreatedBy())
                .map(User::getName)
                .orElse("Neznámy používateľ");

        return convertToTaskResponse(updatedTask, creatorName);
    }

    public void deleteTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new RuntimeException("Úloha nebola nájdená: " + taskId);
        }
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