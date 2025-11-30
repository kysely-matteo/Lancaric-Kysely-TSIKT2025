package sk.front.front.model;

import java.time.LocalDateTime;

public class Task {
    private Long taskId;
    private Long groupId;
    private Long createdBy;
    private String title;
    private String description;
    private String status;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private String creatorName;

    public Task() {}

    public Task(Long taskId, Long groupId, Long createdBy, String title,
                String description, String status, LocalDateTime deadline,
                LocalDateTime createdAt, String creatorName) {
        this.taskId = taskId;
        this.groupId = groupId;
        this.createdBy = createdBy;
        this.title = title;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
        this.createdAt = createdAt;
        this.creatorName = creatorName;
    }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    @Override
    public String toString() {
        return title + " (" + status + ")";
    }
}