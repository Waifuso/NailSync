package phong.demo.Controller;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployeeReposittory employeeReposittory;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private ProfileService profileService;

    @InjectMocks
    private User_controller controller;

    private User user;
    private Profile profile;
    private Ranking ranking;

    @BeforeEach
    void setup() {
        // Prepare ranking
        ranking = Ranking.GOLD;

        // Prepare profile
        profile = new Profile();
        profile.setPhone("555-1234");
        profile.setFirstName("Alice");
        profile.setLastName("Liddell");
        profile.setRanking(ranking);

        // Prepare user
        user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setJoinedDate(LocalDate.of(2025, 5, 1));
        user.setTotalSpend(100);
        user.setProfile(profile);
    }


    // --- nextRanking ---

    @Test
    void nextRanking_userNotFound_returns404() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = controller.nextRanking(1L);

        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void nextRanking_userFound_invokesServiceAndReturnsGoal() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileService.nextRankingestimate(100, ranking))
                .thenReturn("Reach Platinum with $200 more");

        ResponseEntity<?> resp = controller.nextRanking(1L);

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo("Reach Platinum with $200 more");
        verify(profileService).nextRankingestimate(100, ranking);
    }

    // --- FindById ---

    @Test
    void findById_notFound_returns404() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = controller.FindById(2L);

        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void findById_found_returnsUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<?> resp = controller.FindById(1L);

        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        User_DTO dto = (User_DTO) resp.getBody();
        assertThat(dto.getUserName()).isEqualTo("alice");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getJoinedDAte()).isEqualTo(user.getJoinedDate());
        assertThat(dto.getProfile()).isEqualTo(profile);
    }

    // --- Allpeople ---

    @Test
    void allpeople_returnsList() {
        List<User> list = Arrays.asList(user, new User());
        when(userRepository.findAll()).thenReturn(list);

        List<User> result = controller.Allpeople();

        assertThat(result).isEqualTo(list);
    }

    // --- findByName ---

    @Test
    void findByName_notFound_returns404() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        ResponseEntity<User> resp = controller.findByName("bob");

    }
}
