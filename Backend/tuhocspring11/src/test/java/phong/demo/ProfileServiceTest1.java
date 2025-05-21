package phong.demo;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import phong.demo.Entity.Profile;
import phong.demo.Entity.Ranking;
import phong.demo.Entity.User;
import phong.demo.Repository.ProfileRepository;
import phong.demo.Repository.UserRepository;
import phong.demo.Service.ProfileService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest1 {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    private ProfileService profileService;
    private User user;
    private Profile profile;

    @BeforeEach
    void setup() {
        // Create the service with only the profileRepository
        profileService = new ProfileService(profileRepository);
        // Manually inject the userRepository mock
        profileService.userRepository = userRepository;

        // Prepare a user and linked profile
        profile = new Profile();
        user = new User();
        user.setId(1L);
        user.setProfile(profile);
        user.setTotalSpend(0);
    }

    // ---------------- estimateRanking ----------------

    @Test
    void estimateRanking_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.estimateRanking(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can not find the user by their id");
    }

    @Test
    void estimateRanking_usageBelow300_returnsBronze() {
        user.setTotalSpend(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(profileService.estimateRanking(1L))
                .isEqualTo(Ranking.BRONZE);
    }

    @Test
    void estimateRanking_usage300to699_returnsSilver() {
        user.setTotalSpend(300);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(profileService.estimateRanking(1L))
                .isEqualTo(Ranking.SILVER);
    }

    @Test
    void estimateRanking_usage700to1499_returnsGold() {
        user.setTotalSpend(700);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(profileService.estimateRanking(1L))
                .isEqualTo(Ranking.GOLD);
    }

    @Test
    void estimateRanking_usage1500to1999_returnsPlatinum() {
        user.setTotalSpend(1500);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(profileService.estimateRanking(1L))
                .isEqualTo(Ranking.PLATINUM);
    }

    @Test
    void estimateRanking_usageAtOrAbove2000_returnsDiamond() {
        user.setTotalSpend(2000);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(profileService.estimateRanking(1L))
                .isEqualTo(Ranking.DIAMOND);

        user.setTotalSpend(5000);
        assertThat(profileService.estimateRanking(1L))
                .isEqualTo(Ranking.DIAMOND);
    }

    // ---------------- updateSpending ----------------

    @Test
    void updateSpending_userNotFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.updateSpending(50, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Can not find the people by the id");
    }

    @Test
    void updateSpending_userExists_updatesAndSaves() {
        user.setTotalSpend(100);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        profileService.updateSpending(50, 1L);

        assertThat(user.getTotalSpend()).isEqualTo(150);
        verify(userRepository).save(user);
    }

    // ---------------- saveProfile ----------------

    @Test
    void saveProfile_invokesRepositorySave() {
        profileService.saveProfile(profile);
        verify(profileRepository).save(profile);
    }

    // ---------------- nextRankingestimate ----------------

    @Test
    void nextRankingestimate_totalBelowMax_returnsRemainingPoints() {
        Ranking r = Ranking.GOLD;
        int total = r.getUpperbound() - 100;
        String expected = " you are currently: "
                + (r.getUpperbound() - total + 1)
                + " points away from the next Ranking";

        assertThat(profileService.nextRankingestimate(total, r))
                .isEqualTo(expected);
    }

    @Test
    void nextRankingestimate_totalAtOrAbove2000_returnsHighestMessage() {
        String msg1 = profileService.nextRankingestimate(2000, Ranking.BRONZE);
        String msg2 = profileService.nextRankingestimate(5000, Ranking.SILVER);

        assertThat(msg1).isEqualTo("You are currently at the highest ranking of us !!!!");
        assertThat(msg2).isEqualTo("You are currently at the highest ranking of us !!!!");
    }
}
