package sk.front.front.model;

import java.time.LocalDateTime;

public class Resource {
    private Long resourceId;
    private Long groupId;
    private Long uploadedBy;
    private String title;
    private String type; // LINK, FILE, DOCUMENT
    private String pathOrUrl;
    private LocalDateTime uploadedAt;
    private String uploaderName;

    public Resource() {}

    public Resource(Long resourceId, Long groupId, Long uploadedBy, String title,
                    String type, String pathOrUrl, LocalDateTime uploadedAt, String uploaderName) {
        this.resourceId = resourceId;
        this.groupId = groupId;
        this.uploadedBy = uploadedBy;
        this.title = title;
        this.type = type;
        this.pathOrUrl = pathOrUrl;
        this.uploadedAt = uploadedAt;
        this.uploaderName = uploaderName;
    }

    // Getters and Setters
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

    public String getUploaderName() { return uploaderName; }
    public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }


    public String getFormattedType() {
        if ("LINK".equals(type)) return "🔗 Odkaz";
        else if ("FILE".equals(type)) return "📁 Súbor";
        else if ("DOCUMENT".equals(type)) return "📄 Dokument";
        else return type;
    }

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
                ", uploaderName='" + uploaderName + '\'' +
                '}';
    }
}