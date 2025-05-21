package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import phong.demo.Controller.NotificationImageController;
import phong.demo.Entity.Notification;
import phong.demo.Repository.NotificationImageRepository;
import phong.demo.Service.NotificationService;
import phong.demo.Service.S3Service;
import phong.demo.Springpro.ResponseMessagge;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationImageControllerTest {

    @Mock private NotificationImageRepository notificationImageRepository;
    @Mock private NotificationService notificationService;
    @Mock private S3Service s3Service;

    @InjectMocks
    private NotificationImageController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ uploadImage - success case
    @Test
    void testUploadImage_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        String caption = "new art";
        String employeeName = "Alice";

        when(notificationImageRepository.findByCaption(caption)).thenReturn(Optional.empty());
        when(s3Service.upload(file)).thenReturn("test.jpg");
        when(s3Service.generatePresignedUrl("test.jpg")).thenReturn("https://s3.com/test.jpg");

        ResponseEntity<?> response = controller.uploadImage(file, employeeName, caption);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).isEqualTo("Upload successful");

        verify(notificationImageRepository).save(any(Notification.class));
        verify(notificationService).sendNotification(contains("just uploaded new image"));
    }

    // ✅ uploadImage - duplicate caption
    @Test
    void testUploadImage_DuplicateCaption() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "image content".getBytes());
        String caption = "duplicate caption";

        when(notificationImageRepository.findByCaption(caption)).thenReturn(Optional.of(new Notification()));

        ResponseEntity<?> response = controller.uploadImage(file, "Bob", caption);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((ResponseMessagge) response.getBody()).getMessage()).contains("please use different caption");

        verify(notificationImageRepository, never()).save(any());
    }

    // ✅ uploadImage - IOException
    @Test
    void testUploadImage_ThrowsIOException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("file", "error.jpg", "image/jpeg", "bad".getBytes());

        when(notificationImageRepository.findByCaption("error caption")).thenReturn(Optional.empty());
        when(s3Service.upload(file)).thenThrow(new IOException("S3 failed"));

        try {
            controller.uploadImage(file, "Charlie", "error caption");
            assert false : "Expected RuntimeException due to IOException";
        } catch (RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(IOException.class);
        }
    }

    // ✅ getImage - returns gallery
    @Test
    void testGetImage_ReturnsAll() {
        List<Notification> mockList = List.of(
                new Notification("Alice", "caption1", "url1"),
                new Notification("Bob", "caption2", "url2")
        );

        when(notificationImageRepository.findAll()).thenReturn(mockList);

        List<Notification> result = controller.getImage();

        assertThat(result).hasSize(2);
        //assertThat(result.get(0).getEmployee()).isEqualTo("Alice");
        assertThat(result.get(1).getCaption()).isEqualTo("caption2");
    }
}
