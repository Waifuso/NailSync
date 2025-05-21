package phong.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import phong.demo.Entity.Notification;
import phong.demo.Repository.NotificationImageRepository;
import phong.demo.Service.NotificationService;
import phong.demo.Service.S3Service;
import phong.demo.Springpro.ResponseMessagge;
import phong.demo.Websocket.RawWebSocketNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/notify")
public class NotificationImageController {

    @Autowired
    private final NotificationImageRepository notificationImageRepository;
    private NotificationService notificationService;
    private S3Service s3Service;

    public NotificationImageController(NotificationImageRepository notificationImageRepository, NotificationService notificationService,S3Service s3Service) {
        this.notificationImageRepository = notificationImageRepository;
        this.notificationService = notificationService;
        this.s3Service = s3Service;
    }

    @PostMapping(path = "/employee/upload")
    public ResponseEntity<?> uploadImage (@RequestParam MultipartFile file, @RequestParam String employeeName, @RequestParam String caption) {
        try {
            Optional<Notification> checkCaption = notificationImageRepository.findByCaption(caption);
            if (checkCaption.isPresent()) {
                return ResponseEntity.badRequest().body(new ResponseMessagge("please use different caption"));
            }

            String filename = s3Service.upload(file);

            String newURL = s3Service.generatePresignedUrl(filename);

            // Save metadata (optional)
            notificationImageRepository.save(new Notification(employeeName, caption, newURL));

            // Send WebSocket notification with STOMP
            notificationService.sendNotification(employeeName + " just uploaded new image. Click to see what's new");

            // Send normal WebSocket notification
            RawWebSocketNotification.sendToAll("🔔 " + employeeName + " just uploaded new image. Click to see what's new");

            return ResponseEntity.ok(new ResponseMessagge("Upload successful"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/gallery")
    public List<Notification> getImage(){
        return notificationImageRepository.findAll();
    }




}
