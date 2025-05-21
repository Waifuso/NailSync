package phong.demo;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import phong.demo.Controller.*;
import phong.demo.Entity.*;
import phong.demo.Repository.*;
import phong.demo.Service.NotificationService;
import phong.demo.Service.S3Service;
import phong.demo.Springpro.ResponseMessagge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;



import java.util.Optional;

import static io.restassured.RestAssured.when;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TuhocspringApplicationTests {

	@LocalServerPort
	int port;

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
		MockitoAnnotations.openMocks(this);
		mockFile = new MockMultipartFile(
				"file", "test.jpg", "image/jpeg", "dummy content".getBytes()
		);

	}

	@Test
	void contextLoads() {
		int result = 1 + 2;
		assertEquals(3, result);
	}

	@Test
	public void signup(){
		when().request("GET", "/api/signup/get").then().statusCode(200);
	}

	@Test
	public void appointments(){
		when().request("GET", "/api/appointments/infoSerandEm").then().statusCode(200);
	}
	@Test
	public void payment(){
		when().request("GET", "/api/payment/userPayment/1").then().statusCode(200);
	}

	@Test
	public void ranking(){
		when().request("GET", "/api/payment/userRank&Points/1").then().statusCode(200);
	}

	@InjectMocks
	private RatingController controller;
	@InjectMocks
	private User_controller UserController;
	@InjectMocks
	private AppointmentController AController;
	@InjectMocks
	private NotificationImageController NotifyController;




	//@Mock
	@Mock(lenient = true)
	private AppointmentRepository appointmentRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AppointmentServiceRepository appointmentServiceRepository;

	@Mock
	private RatingRepository ratingRepository;

	@Mock
	private NotificationImageRepository notificationImageRepository;

	@Mock
	private S3Service s3Service;

	@Mock
	private NotificationService notificationService;

	private MockMultipartFile mockFile;

	@Test
	void testMissingStar() {
		ResponseEntity<?> response = controller.ratingByCustomer(null, "Good!", 1L);

		assertEquals(400, response.getStatusCodeValue());
		assertEquals("Please fill up the star for rating", ((ResponseMessagge) response.getBody()).getMessage());
	}

	@Test
	void testCancelAppointment_NotFound() {
		// Arrange
		Long appointmentId = 2L;
		when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

		// Act
		ResponseEntity<?> response = AController.cancelAppointment(appointmentId);

		// Assert
		assertEquals(400, response.getStatusCodeValue());
		assertTrue(response.getBody() instanceof ResponseMessagge);
		assertEquals("Appointment not found", ((ResponseMessagge) response.getBody()).getMessage());

		verify(appointmentRepository, never()).delete(any());
		verify(appointmentServiceRepository, never()).deleteById(any());
	}
	@Test
	void testSendNotifications_ReturnsSameMessage() {
		// Arrange
		NotificationController controller = new NotificationController();
		String input = "Hello WebSocket!";

		// Act
		String result = controller.sendNotifications(input);

		// Assert
		assertEquals(input, result);
	}


}

