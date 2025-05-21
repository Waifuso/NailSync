package phong.demo;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import phong.demo.Controller.User_controller;
import phong.demo.DTO.JSOn_objectString;
import phong.demo.DTO.User_DTO;
import phong.demo.Entity.Profile;
import phong.demo.Entity.Ranking;
import phong.demo.Entity.User;
import phong.demo.Repository.EmployeeReposittory;
import phong.demo.Repository.ProfileRepository;
import phong.demo.Repository.UserRepository;
import phong.demo.Service.ProfileService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private EmployeeReposittory employeeReposittory;
    @Mock private ProfileRepository profileRepository;
    @Mock private ProfileService profileService;

    @InjectMocks private User_controller controller;

    private User user;
    private Profile profile;
    private Ranking ranking;

    @BeforeEach
    void setup() {
        ranking = Ranking.SILVER;
        profile = new Profile();
        profile.setRanking(ranking);
        profile.setPhone("123");
        profile.setFirstName("John");
        profile.setLastName("Doe");

        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("pass");
        user.setJoinedDate(LocalDate.of(2025, 5, 1));
        user.setTotalSpend(700);
        user.setProfile(profile);
    }

    @Test
    void nextRanking_notFound_returns404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseEntity<?> resp = controller.nextRanking(1L);
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void nextRanking_found_returnsGoal() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileService.nextRankingestimate(700, ranking)).thenReturn("Goal");
        ResponseEntity<?> resp = controller.nextRanking(1L);
        assertThat(resp.getBody()).isEqualTo("Goal");
    }

    @Test
    void findById_notFound_returns404() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        ResponseEntity<?> resp = controller.FindById(2L);
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void findById_found_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<?> resp = controller.FindById(1L);
        User_DTO dto = (User_DTO) resp.getBody();
        assertThat(dto.getUserName()).isEqualTo("john");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void allpeople_returnsList() {
        List<User> list = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(list);
        List<User> resp = controller.Allpeople();
        assertThat(resp).isEqualTo(list);
    }

    @Test
    void findByName_notFound_returns404() {
        when(userRepository.findByUsername("jane")).thenReturn(Optional.empty());
        ResponseEntity<User> resp = controller.findByName("jane");
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void findByName_found_returnsUser() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        ResponseEntity<User> resp = controller.findByName("john");
        assertThat(resp.getBody()).isEqualTo(user);
    }

    @Test
    void createUser_savesAndReturns() {
        when(userRepository.save(user)).thenReturn(user);
        User resp = controller.CreateteUser(user);
        assertThat(resp).isEqualTo(user);
    }

    @Test
    void editUser_notFound_returns404() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        ResponseEntity<?> resp = controller.EditUser(3L, new User());
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void editUser_usernameExists_returns400() {
        User upd = new User(); upd.setUsername("john");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("john")).thenReturn(true);
        ResponseEntity<?> resp = controller.EditUser(1L, upd);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void editUser_passwordSame_returns400() {
        user.setPassword("pass");
        User upd = new User(); upd.setPassword("pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername(any())).thenReturn(false);
        ResponseEntity<?> resp = controller.EditUser(1L, upd);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void editUser_emailExists_returns400() {
        User upd = new User(); upd.setUsername("new"); upd.setPassword("new"); upd.setEmail("john@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("new")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        ResponseEntity<?> resp = controller.EditUser(1L, upd);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void editUser_success_returns200() {
        User upd = new User(); upd.setUsername("john2"); upd.setPassword("new"); upd.setEmail("j2@e.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("john2")).thenReturn(false);
        when(userRepository.existsByEmail("j2@e.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        ResponseEntity<?> resp = controller.EditUser(1L, upd);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void getProfile_notFound_returns404() {
        when(userRepository.findById(4L)).thenReturn(Optional.empty());
        ResponseEntity<?> resp = controller.GetProfile(4L);
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void getProfile_found_returnsProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<?> resp = controller.GetProfile(1L);
        assertThat(resp.getBody()).isEqualTo(profile);
    }

    @Test
    void createProfile_nullProfile_returns400() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ResponseEntity<?> resp = controller.createProfile(1L, null);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void createProfile_phoneExists_returns400() {
        Profile upd = new Profile(); upd.setPhone("123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByPhone("123")).thenReturn(true);
        ResponseEntity<?> resp = controller.createProfile(1L, upd);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void createProfile_success_returns200() {
        Profile upd = new Profile(); upd.setPhone("456"); upd.setFirstName("A"); upd.setLastName("B");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileRepository.existsByPhone("456")).thenReturn(false);
        when(profileService.estimateRanking(1L)).thenReturn(ranking);
        when(profileRepository.save(any())).thenReturn(upd);
        ResponseEntity<?> resp = controller.createProfile(1L, upd);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void testUpdate_success_returns200() {
        Profile p = new Profile();
        when(profileRepository.findById(1L)).thenReturn(Optional.of(p));
        doNothing().when(profileService).updateSpending(10, 1L);
        when(profileService.estimateRanking(1L)).thenReturn(ranking);
        when(profileRepository.save(p)).thenReturn(p);
        ResponseEntity<?> resp = controller.testUpdate(1L,10);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void testUpdate_notFound_returns404() {
        Profile p = new Profile();
        when(profileRepository.findById(2L)).thenReturn(Optional.of(p));
        doThrow(new EntityNotFoundException("no")).when(profileService).updateSpending(5,2L);
        ResponseEntity<?> resp = controller.testUpdate(2L,5);
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void deleteProfile_invokesRepository() {
        controller.Deleteprofile(99L);
        verify(profileRepository).deleteById(99L);
    }
}

