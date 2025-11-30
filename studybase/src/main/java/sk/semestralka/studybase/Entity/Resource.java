package sk.semestralka.studybase.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resources")
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(nullable = false)
    private String title;

    private String type; // LINK, FILE, DOCUMENT

    @Column(name = "path_or_url")
    private String pathOrUrl;

    private LocalDateTime uploadedAt;

    public Resource() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Resource(Long groupId, Long uploadedBy, String title, String type, String pathOrUrl) {
        this();
        this.groupId = groupId;
        this.uploadedBy = uploadedBy;
        this.title = title;
        this.type = type;
        this.pathOrUrl = pathOrUrl;
    }


    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(Long uploadedBy) { this.uploadedBy = uploadedBy; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPathOrUrl() { return pathOrUrl; }
    public void setPathOrUrl(String pathOrUrl) { this.pathOrUrl = pathOrUrl; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    @Override
    public String toString() {
        return "Resource{" +
                "resourceId=" + resourceId +
                ", groupId=" + groupId +
                ", uploadedBy=" + uploadedBy +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", pathOrUrl='" + pathOrUrl + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}