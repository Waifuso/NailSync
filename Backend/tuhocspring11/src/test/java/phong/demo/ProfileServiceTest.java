package phong.demo;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import phong.demo.Entity.Profile;
import phong.demo.Entity.Ranking;
import phong.demo.Entity.User;
import phong.demo.Repository.ProfileRepository;
import phong.demo.Repository.UserRepository;
import phong.demo.Service.ProfileService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    private ProfileRepository profileRepository;
    private UserRepository userRepository;
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        userRepository = mock(UserRepository.class);
        profileService = new ProfileService(profileRepository);
        profileService.userRepository = userRepository;
    }

    @Test
    void testEstimateRanking_Bronze() {
        User user = new User();
        user.setTotalSpend(100);
        user.setProfile(new Profile());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Ranking result = profileService.estimateRanking(1L);
        assertEquals(Ranking.BRONZE, result);
    }

    @Test
    void testEstimateRanking_Silver() {
        User user = new User();
        user.setTotalSpend(400);
        user.setProfile(new Profile());

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Ranking result = profileService.estimateRanking(2L);
        assertEquals(Ranking.SILVER, result);
    }

    @Test
    void testEstimateRanking_NotFound() {
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> profileService.estimateRanking(3L));
    }

    @Test
    void testUpdateSpending_success() {
        User user = new User();
        user.setTotalSpend(200);

        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        profileService.updateSpending(100, 4L);
        assertEquals(300, user.getTotalSpend());
    }

    @Test
    void testUpdateSpending_userNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> profileService.updateSpending(100, 5L));
    }

    @Test
    void testNextRankingEstimate_NotMax() {
        String message = profileService.nextRankingestimate(1450, Ranking.GOLD);
        assertFalse(message.contains("51 points away"));
    }

    @Test
    void testNextRankingEstimate_MaxRank() {
        String message = profileService.nextRankingestimate(2100, Ranking.DIAMOND);
        assertEquals("You are currently at the highest ranking of us !!!!", message);
    }
}

