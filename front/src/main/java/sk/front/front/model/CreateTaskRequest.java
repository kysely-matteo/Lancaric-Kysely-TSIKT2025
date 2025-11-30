package sk.front.front.model;

import java.time.LocalDateTime;

public class CreateTaskRequest {
    private String title;
    private String description;
    private LocalDateTime deadline;

    public CreateTaskRequest() {}

    public CreateTaskRequest(String title, String description, LocalDateTime deadline) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}