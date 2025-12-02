package sk.semestralka.studybase.DTO;

import java.time.LocalDateTime;

public class GroupResponse {
    private Long groupId;
    private String name;
    private String description;
    private Long createdBy;
    private LocalDateTime createdAt;
    private int memberCount;


    public GroupResponse() {}

    public GroupResponse(Long groupId, String name, String description,
                         Long createdBy, LocalDateTime createdAt, int memberCount) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
    }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    @Override
    public String toString() {
        return "GroupResponse{" +
                "groupId=" + groupId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", memberCount=" + memberCount +
                '}';
    }
}