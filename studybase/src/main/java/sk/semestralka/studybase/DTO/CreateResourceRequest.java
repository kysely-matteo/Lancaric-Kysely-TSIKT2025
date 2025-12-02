package sk.semestralka.studybase.DTO;

public class CreateResourceRequest {
    private String title;
    private String type;
    private String pathOrUrl;

    public CreateResourceRequest() {}

    public CreateResourceRequest(String title, String type, String pathOrUrl) {
        this.title = title;
        this.type = type;
        this.pathOrUrl = pathOrUrl;
    }


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPathOrUrl() { return pathOrUrl; }
    public void setPathOrUrl(String pathOrUrl) { this.pathOrUrl = pathOrUrl; }

    @Override
    public String toString() {
        return "CreateResourceRequest{" +
                "title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", pathOrUrl='" + pathOrUrl + '\'' +
                '}';
    }
}