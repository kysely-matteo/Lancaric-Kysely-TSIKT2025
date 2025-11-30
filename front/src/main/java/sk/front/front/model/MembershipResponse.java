package sk.front.front.model;

import java.time.LocalDateTime;

public class MembershipResponse {
    private Long membershipId;
    private Long userId;
    private Long groupId;
    private String role;
    private LocalDateTime joinedAt;
    private String userName;

    public MembershipResponse() {}

    public MembershipResponse(Long membershipId, Long userId, Long groupId,
                              String role, LocalDateTime joinedAt, String userName) {
        this.membershipId = membershipId;
        this.userId = userId;
        this.groupId = groupId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.userName = userName;
    }


    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    @Override
    public String toString() {
        return userName + " (" + role + ")";
    }
}