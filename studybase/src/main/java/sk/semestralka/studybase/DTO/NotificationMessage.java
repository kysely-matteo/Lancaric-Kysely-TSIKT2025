package sk.semestralka.studybase.DTO;

public class NotificationMessage {
    private String type;  // "TASK_CREATED", "RESOURCE_UPLOADED", "MEMBER_JOINED"
    private String title;
    private String message;
    private Long groupId;
    private Long userId;
    private String userName;
    private String timestamp;

    // Konštruktory, gettery, settery

    public NotificationMessage() {
    }

    public NotificationMessage(String type, String title, String message, Long groupId, Long userId, String userName, String timestamp) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.groupId = groupId;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "[" + type + "] " + title + ": " + message + " (" + timestamp + ")";
    }


    // Gettery a settery pre všetky polia
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}