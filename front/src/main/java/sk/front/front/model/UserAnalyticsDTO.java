package sk.front.front.model;

import java.util.Map;

public class UserAnalyticsDTO {
    private Long userId;
    private String userName;
    private int totalTasksCreated;
    private Map<String, Long> taskStatusStats;
    private long resourcesUploaded;
    private int totalGroups;
    private long adminGroups;
    private Long weeklyActivities;

    // Getters and Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getTotalTasksCreated() { return totalTasksCreated; }
    public void setTotalTasksCreated(int totalTasksCreated) { this.totalTasksCreated = totalTasksCreated; }

    public Map<String, Long> getTaskStatusStats() { return taskStatusStats; }
    public void setTaskStatusStats(Map<String, Long> taskStatusStats) { this.taskStatusStats = taskStatusStats; }

    public long getResourcesUploaded() { return resourcesUploaded; }
    public void setResourcesUploaded(long resourcesUploaded) { this.resourcesUploaded = resourcesUploaded; }

    public int getTotalGroups() { return totalGroups; }
    public void setTotalGroups(int totalGroups) { this.totalGroups = totalGroups; }

    public long getAdminGroups() { return adminGroups; }
    public void setAdminGroups(long adminGroups) { this.adminGroups = adminGroups; }

    public Long getWeeklyActivities() { return weeklyActivities; }
    public void setWeeklyActivities(Long weeklyActivities) { this.weeklyActivities = weeklyActivities; }
}