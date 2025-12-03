package sk.semestralka.studybase.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "memberships")
public class Membership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long membershipId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private String role = "MEMBER"; // MEMBER, ADMIN, OWNER

    private LocalDateTime joinedAt;

    // Konštruktory
    public Membership() {
        this.joinedAt = LocalDateTime.now();
    }

    public Membership(Long userId, Long groupId, String role) {
        this();
        this.userId = userId;
        this.groupId = groupId;
        this.role = role;
    }

    // Getters a Setters
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

    @Override
    public String toString() {
        return "Membership{" +
                "membershipId=" + membershipId +
                ", userId=" + userId +
                ", groupId=" + groupId +
                ", role='" + role + '\'' +
                ", joinedAt=" + joinedAt +
                '}';
    }
}