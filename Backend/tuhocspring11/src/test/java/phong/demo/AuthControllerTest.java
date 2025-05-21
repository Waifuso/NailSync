package phong.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.Authcontroller;
import phong.demo.DTO.*;
import phong.demo.Entity.*;
import phong.demo.Repository.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthcontrollerTest {

    @Mock private UserRepository userRepository;
    @Mock private EmployeeReposittory employeeReposittory;
    @Mock private AdminRepository adminRepository;

    @InjectMocks
    private Authcontroller controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ User login (username)
    @Test
    void testUserLogin_Success() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Loginrequest request = new Loginrequest("testuser", "password123");

        ResponseEntity<String> response = controller.login(request);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Login successful!");
    }

    @Test
    void testUserLogin_WrongPassword() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("correctpass");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Loginrequest request = new Loginrequest("testuser", "wrongpass");

        ResponseEntity<String> response = controller.login(request);
        assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void testUserLogin_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Loginrequest request = new Loginrequest("unknown", "pass");

        ResponseEntity<String> response = controller.login(request);
        assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    // ✅ User login by email
    @Test
    void testLoginByEmail_Success() {
        User user = new User(); user.setId(1L); user.setEmail("abc@test.com"); user.setPassword("123");
        when(userRepository.findByEmail("abc@test.com")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.loginByEmail("abc@test.com", "123");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
       // assertThat(((Json_object_withID) response.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    void testLoginByEmail_Invalid() {
        when(userRepository.findByEmail("bad@email.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.loginByEmail("bad@email.com", "wrong");

        assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    // ✅ Employee login
    @Test
    void testEmployeeLogin_Success() {
        Employee emp = new Employee(); emp.setUsername("emp1"); emp.setServicePassword("abc123"); emp.setId(10L);
        when(employeeReposittory.findByUsername("emp1")).thenReturn(Optional.of(emp));

        Loginrequest req = new Loginrequest("emp1", "abc123");

       // ResponseEntity<?> response = controller.EmployeeLogin(req);

       // assertThat(response.getStatusCodeValue()).isEqualTo(200);
       // assertThat(((Json_object_withID) response.getBody()).getId()).isEqualTo(10L);
    }

    @Test
    void testEmployeeLogin_WrongPassword() {
        Employee emp = new Employee(); emp.setUsername("emp1"); emp.setServicePassword("correct");
        when(employeeReposittory.findByUsername("emp1")).thenReturn(Optional.of(emp));

        Loginrequest req = new Loginrequest("emp1", "wrong");

        //ResponseEntity<?> response = controller.EmployeeLogin(req);
        //assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void testEmployeeLogin_NotFound() {
        when(employeeReposittory.findByUsername("unknown")).thenReturn(Optional.empty());

        Loginrequest req = new Loginrequest("unknown", "pass");

        //ResponseEntity<?> response = controller.EmployeeLogin(req);
       // assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    // ✅ Admin login
    @Test
    void testAdminLogin_Success() {
        Admin admin = new Admin(); admin.setUsername("admin1"); admin.setPassword("adminpass"); admin.setId(5L);
        when(adminRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));

        Loginrequest req = new Loginrequest("admin1", "adminpass");

        //ResponseEntity<?> response = controller.AdminLogin(req);
        //assertThat(response.getStatusCodeValue()).isEqualTo(200);
       // assertThat(((Json_object_withID) response.getBody()).getId()).isEqualTo(5L);
    }

    @Test
    void testAdminLogin_WrongPassword() {
        Admin admin = new Admin(); admin.setUsername("admin1"); admin.setPassword("correct");
        when(adminRepository.findByUsername("admin1")).thenReturn(Optional.of(admin));

        Loginrequest req = new Loginrequest("admin1", "wrong");

       // ResponseEntity<?> response = controller.AdminLogin(req);
      //  assertThat(response.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void testAdminLogin_NotFound() {
        when(adminRepository.findByUsername("missing")).thenReturn(Optional.empty());

        Loginrequest req = new Loginrequest("missing", "x");

       // ResponseEntity<?> response = controller.AdminLogin(req);
       // assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    // ✅ Admin signup
    @Test
    void testAdminSignup_Success() {
        Admin admin = new Admin(); admin.setUsername("newadmin"); admin.setEmail("admin@mail.com"); admin.setId(999L);

        when(adminRepository.existsByUsername("newadmin")).thenReturn(false);
        when(adminRepository.existsByEmail("admin@mail.com")).thenReturn(false);
        when(adminRepository.save(admin)).thenReturn(admin);

       // ResponseEntity<?> response = controller.adminSignup(admin);

        //assertThat(response.getStatusCodeValue()).isEqualTo(201);
       // assertThat(((Json_object_withID) response.getBody()).getId()).isEqualTo(999L);
    }

    @Test
    void testAdminSignup_UsernameExists() {
        Admin admin = new Admin(); admin.setUsername("admin");
        when(adminRepository.existsByUsername("admin")).thenReturn(true);

        ResponseEntity<?> response = controller.adminSignup(admin);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
      //  assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("Username already exists");
    }

    @Test
    void testAdminSignup_EmailMissing() {
        Admin admin = new Admin(); admin.setUsername("x"); admin.setEmail("  "); // empty email

        ResponseEntity<?> response = controller.adminSignup(admin);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
      //  assertThat(((JSOn_objectString) response.getBody()).getMess()).contains("Email cannot be null");
    }
}
