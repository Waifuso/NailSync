package phong.demo;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import phong.demo.Entity.Payment;
import phong.demo.Repository.PaymentRepository;
import phong.demo.Service.DailyFinanceService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class DailyFinanceServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private DailyFinanceService dailyFinanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFinance_shouldReturnCorrectSumForGivenDate() {
        LocalDate date = LocalDate.of(2025, 5, 7);

        Payment payment1 = new Payment();
        payment1.setAmount(100);

        Payment payment2 = new Payment();
        payment2.setAmount(250);

        when(paymentRepository.findAllByDate(date)).thenReturn(List.of(payment1, payment2));

        double result = dailyFinanceService.getFinance(date);

        assertEquals(350.0, result);
    }

    @Test
    void getFinance_shouldReturnZeroWhenNoPayments() {
        LocalDate date = LocalDate.of(2025, 5, 7);

        when(paymentRepository.findAllByDate(date)).thenReturn(List.of());

        double result = dailyFinanceService.getFinance(date);

        assertEquals(0.0, result);
    }
}
