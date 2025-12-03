package sk.semestralka.studybase.DTO;

public class UpdateTaskRequest {
    private String title;
    private String description;
    private String status;
    private String deadline;


    public UpdateTaskRequest() {}

    public UpdateTaskRequest(String title, String description, String status, String deadline) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.deadline = deadline;
    }


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    @Override
    public String toString() {
        return "UpdateTaskRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", deadline='" + deadline + '\'' +
                '}';
    }
}