package sk.semestralka.studybase.Service;
import sk.semestralka.studybase.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import sk.semestralka.studybase.DTO.NotificationMessage;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToGroup(Long groupId, NotificationMessage notification) {
        System.out.println("DEBUG: Posielam notifikáciu do skupiny " + groupId);
        System.out.println("DEBUG: Typ: " + notification.getType());
        System.out.println("DEBUG: Destinácia: /topic/group/" + groupId);
        System.out.println("DEBUG: Notifikácia: " + notification.getMessage());

        // Posli notifikáciu všetkým prihláseným členom skupiny
        String destination = "/topic/group/" + groupId;
        messagingTemplate.convertAndSend(destination, notification);
    }

    public void sendNotificationToUser(Long userId, NotificationMessage notification) {
        // Posli notifikáciu konkrétnemu používateľovi
        String destination = "/topic/user/" + userId;
        messagingTemplate.convertAndSend(destination, notification);
    }
}