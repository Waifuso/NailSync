package phong.demo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import phong.demo.Entity.User;
import phong.demo.Repository.UserRepository;
import phong.demo.Service.Userservice;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class UserserviceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private Userservice userservice;

    private List<User> mockUsers;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock users with various joined dates
        User user1 = new User();
        user1.setJoinedDate(LocalDate.of(2025, 5, 5));

        User user2 = new User();
        user2.setJoinedDate(LocalDate.of(2025, 5, 6));

        User user3 = new User();
        user3.setJoinedDate(LocalDate.of(2025, 5, 6));

        mockUsers = Arrays.asList(user1, user2, user3);
    }

    @Test
    public void testFindNewUser_inRange_shouldReturnCorrectCount() {
        when(userRepository.findAll()).thenReturn(mockUsers);

        LocalDate startDate = LocalDate.of(2025, 5, 5);
        LocalDate endDate = LocalDate.of(2025, 5, 6);

        int newUsers = userservice.findNewUser(startDate, endDate);

        assertEquals(3, newUsers); // user1 (1) + user2 & user3 (2)
    }

    @Test
    public void testFindNewUser_outOfRange_shouldReturnZero() {
        when(userRepository.findAll()).thenReturn(mockUsers);

        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 2);

        int newUsers = userservice.findNewUser(startDate, endDate);

        assertEquals(0, newUsers);
    }

    @Test
    public void testFindNewUser_singleDay_shouldReturnCorrectCount() {
        when(userRepository.findAll()).thenReturn(mockUsers);

        LocalDate date = LocalDate.of(2025, 5, 6);

        int newUsers = userservice.findNewUser(date, date);

        assertEquals(2, newUsers); // user2 and user3
    }
}
