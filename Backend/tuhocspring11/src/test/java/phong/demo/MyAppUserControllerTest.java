package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import phong.demo.Entity.User;
import phong.demo.Repository.UserRepository;
import phong.demo.Springpro.MyAppUserController;
import phong.demo.Springpro.RandomNumberService;
import phong.demo.Springpro.ResponseMessagge;
import phong.demo.Springpro.SendEmailService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MyAppUserControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private SendEmailService sendEmailService;
    @Mock private RandomNumberService randomNumberService;

    @InjectMocks
    private MyAppUserController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ Test /resetPassword
    @Test
    void testSendResetCode_Success() {
        User user = new User();
        user.setEmail("test@example.com");

        User dbUser = new User();
        dbUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(dbUser));
        when(randomNumberService.generateRandomNumber(100)).thenReturn(12345);

        ResponseEntity<ResponseMessagge> response = controller.sendResetCode(user);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getMessage()).contains("Vertification code sent");
        verify(sendEmailService).SendEmail(eq("test@example.com"), contains("12345"), anyString());
        verify(userRepository).save(dbUser);
    }

    @Test
    void testSendResetCode_EmailNotFound() {
        User user = new User();
        user.setEmail("missing@example.com");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResponseEntity<ResponseMessagge> response = controller.sendResetCode(user);
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).contains("Email not found");
    }

    // ✅ Test /enterCode
    @Test
    void testEnterResetCode_Correct() {
        User input = new User();
        input.setEmail("user@example.com");
        input.setResetNums(1111);

        User dbUser = new User();
        dbUser.setEmail("user@example.com");
        dbUser.setResetNums(1111);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(dbUser));

        ResponseEntity<ResponseMessagge> response = controller.enterResetCode(input);
        assertThat(response.getBody().getMessage()).isEqualTo("Great! Code is correct.");
    }

    @Test
    void testEnterResetCode_Wrong() {
        User input = new User();
        input.setEmail("user@example.com");
        input.setResetNums(9999);

        User dbUser = new User();
        dbUser.setEmail("user@example.com");
        dbUser.setResetNums(1111);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(dbUser));

        ResponseEntity<ResponseMessagge> response = controller.enterResetCode(input);
        assertThat(response.getBody().getMessage()).isEqualTo("Not correct");
    }

    // ✅ Test /reg
    @Test
    void testCreateUser_NewUser() {
        User user = new User();
        user.setEmail("new@user.com");
        user.setUsername("newUser");

        when(userRepository.findByEmail("new@user.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        ResponseEntity<ResponseMessagge> response = controller.createUser(user);
        assertThat(response.getBody().getMessage()).contains("sign up success");
    }

    @Test
    void testCreateUser_EmailConflict() {
        User user = new User();
        user.setEmail("exist@user.com");

        when(userRepository.findByEmail("exist@user.com")).thenReturn(Optional.of(new User()));

        ResponseEntity<ResponseMessagge> response = controller.createUser(user);
        assertThat(response.getStatusCodeValue()).isEqualTo(409);
    }

    // ✅ Test /del
    @Test
    void testDeleteUser_ByEmailSuccess() {
        User user = new User();
        user.setEmail("toDelete@user.com");

        User dbUser = new User();
        dbUser.setEmail("toDelete@user.com");

        when(userRepository.findByEmail("toDelete@user.com")).thenReturn(Optional.of(dbUser));

        ResponseEntity<ResponseMessagge> response = controller.deleteUser(user);
        assertThat(response.getBody().getMessage()).contains("delete success");
        verify(userRepository).delete(dbUser);
    }

    @Test
    void testDeleteUser_NotFound() {
        User user = new User();
        user.setEmail("unknown@user.com");

        when(userRepository.findByEmail("unknown@user.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername(null)).thenReturn(Optional.empty());

        ResponseEntity<ResponseMessagge> response = controller.deleteUser(user);
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("delete failure");
    }
}

