package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import phong.demo.Controller.ImageController;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.JSon_Object;
import phong.demo.Entity.Image;
import phong.demo.Repository.ImageRepository;
import phong.demo.Service.S3Service;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ImageControllerTest {

    @Mock private S3Service s3Service;
    @Mock private ImageRepository imageRepository;

    @InjectMocks
    private ImageController imageController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ POST /upload - success
    @Test
    void testUploadImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "nail.jpg", "image/jpeg", "fake-image-data".getBytes());

        //when(file.isEmpty()).thenReturn(false);
        when(imageRepository.existsByImageName("nail1")).thenReturn(false);
        when(s3Service.upload(file)).thenReturn("s3://nail/nail.jpg");

        ResponseEntity<String> response = imageController.uploadImage(file, "nail1");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).contains("Upload successful");
        verify(imageRepository).save(any(Image.class));
    }

    // ❌ POST /upload - file is empty
    @Test
    void testUploadImage_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        ResponseEntity<String> response = imageController.uploadImage(emptyFile, "any");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).contains("no file provided");
    }

    // ❌ POST /upload - duplicate image name
    @Test
    void testUploadImage_DuplicateName() {
        MockMultipartFile file = new MockMultipartFile("file", "nail.jpg", "image/jpeg", "data".getBytes());

        //when(file.isEmpty()).thenReturn(false);
        when(imageRepository.existsByImageName("nail1")).thenReturn(true);

        ResponseEntity<String> response = imageController.uploadImage(file, "nail1");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).contains("name has already been used");
    }

    // ✅ GET /image/loader/{imageName}
    @Test
    void testGetImage_Found() {
        Image image = new Image("nail1", "s3://nail/nail.jpg");
        when(imageRepository.findByImageName("nail1")).thenReturn(Optional.of(image));
        when(s3Service.generatePresignedUrl("s3://nail/nail.jpg")).thenReturn("https://presigned.url");

        ResponseEntity<?> response = imageController.getImage("nail1");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        //assertThat(((JSOn_objectString) response.getBody()).getMess()).isEqualTo("https://presigned.url");
    }

    @Test
    void testGetImage_NotFound() {
        when(imageRepository.findByImageName("unknown")).thenReturn(Optional.empty());

        ResponseEntity<?> response = imageController.getImage("unknown");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(" can not find the image");
    }

    // ✅ GET /image/loader/getALlNails
    @Test
    void testGetAllNailImages() {
        Image img1 = new Image("art1", "s3://img1");
        Image img2 = new Image("art2", "s3://img2");

        when(imageRepository.findAll()).thenReturn(List.of(img1, img2));
        when(s3Service.generatePresignedUrl("s3://img1")).thenReturn("url1");
        when(s3Service.generatePresignedUrl("s3://img2")).thenReturn("url2");

        ResponseEntity<?> response = imageController.getallNailImage();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        JSon_Object<?> body = (JSon_Object<?>) response.getBody();
        //List<Map<String, String>> imageList = (List<Map<String, String>>) body.getList();

//        assertThat(imageList).hasSize(2);
//        assertThat(imageList.get(0)).containsKey("art1");
//        assertThat(imageList.get(1)).containsKey("art2");
    }
}
