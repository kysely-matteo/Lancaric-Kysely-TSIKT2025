package sk.semestralka.studybase.DTO;

import java.util.List;
import java.util.Map;

public class GroupAnalyticsDTO {
    private Long groupId;
    private int totalTasks;
    private int openTasks;
    private int inProgressTasks;
    private int completedTasks;
    private double completionRate;
    private int totalResources;
    private Map<String, Long> resourceTypeStats;
    private List<MemberActivityDTO> memberActivities;
    private Double averageCompletionTime;
    private String month;

    // Getters and Setters
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getOpenTasks() {
        return openTasks;
    }

    public void setOpenTasks(int openTasks) {
        this.openTasks = openTasks;
    }

    public int getInProgressTasks() {
        return inProgressTasks;
    }

    public void setInProgressTasks(int inProgressTasks) {
        this.inProgressTasks = inProgressTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public int getTotalResources() {
        return totalResources;
    }

    public void setTotalResources(int totalResources) {
        this.totalResources = totalResources;
    }

    public Map<String, Long> getResourceTypeStats() {
        return resourceTypeStats;
    }

    public void setResourceTypeStats(Map<String, Long> resourceTypeStats) {
        this.resourceTypeStats = resourceTypeStats;
    }

    public List<MemberActivityDTO> getMemberActivities() {
        return memberActivities;
    }

    public void setMemberActivities(List<MemberActivityDTO> memberActivities) {
        this.memberActivities = memberActivities;
    }

    public Double getAverageCompletionTime() {
        return averageCompletionTime;
    }

    public void setAverageCompletionTime(Double averageCompletionTime) {
        this.averageCompletionTime = averageCompletionTime;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}