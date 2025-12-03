package sk.semestralka.studybase.DTO;

public class MemberActivityDTO {
    private Long userId;
    private String userName;
    private String role;
    private long tasksCreated;
    private long tasksCompleted;
    private long resourcesUploaded;
    private long totalActivities;
    private int activityScore;

    // Getters and Setters
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getTasksCreated() {
        return tasksCreated;
    }

    public void setTasksCreated(long tasksCreated) {
        this.tasksCreated = tasksCreated;
    }

    public long getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(long tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public long getResourcesUploaded() {
        return resourcesUploaded;
    }

    public void setResourcesUploaded(long resourcesUploaded) {
        this.resourcesUploaded = resourcesUploaded;
    }

    public long getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(long totalActivities) {
        this.totalActivities = totalActivities;
    }

    public int getActivityScore() {
        return activityScore;
    }

    public void setActivityScore(int activityScore) {
        this.activityScore = activityScore;
    }
}