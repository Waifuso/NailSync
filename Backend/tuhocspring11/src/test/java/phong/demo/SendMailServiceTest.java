package phong.demo;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.MessagingException;
import phong.demo.Springpro.SendEmailService;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class SendEmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private SendEmailService sendEmailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> simpleMailCaptor;

    @BeforeEach
    void setUp() throws Exception {
        // Inject the private `fromEmailId` field using reflection
        Field field = SendEmailService.class.getDeclaredField("fromEmailId");
        field.setAccessible(true);
        field.set(sendEmailService, "test@nailsync.com");
    }


    @Test
    void testSendEmail_plainText_success() {
        // Arrange
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("user@example.com");
        message.setSubject("Hello");
        message.setText("This is a test");
        message.setFrom("test@nailsync.com");

        doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act
        sendEmailService.SendEmail("user@example.com", "This is a test", "Hello");

        // Assert
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendEmail_plainText() {
        sendEmailService.SendEmail("recipient@test.com", "Body", "Subject");

        verify(javaMailSender).send(simpleMailCaptor.capture());

        SimpleMailMessage message = simpleMailCaptor.getValue();
        assert message.getTo()[0].equals("recipient@test.com");
        assert message.getSubject().equals("Subject");
        assert message.getText().equals("Body");
        assert message.getFrom().equals("test@nailsync.com");
    }

    @Test
    void testSendEmailWithHtml() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        try {
            sendEmailService.sendEmailWithHtml("recipient@test.com", "Appointment Confirmed", 123456);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testPaymentSending() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        try {
            sendEmailService.paymentSending("recipient@test.com", "Payment Confirmed", 49.99);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testDailyReport() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        try {
            sendEmailService.dailyReport("recipient@test.com", "Daily Summary", 100.0);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testRedeemCodeEmail() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        try {
            sendEmailService.redeemCodeEmail("recipient@test.com", "Redeem Your Code", "COUPON123", 150);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void testSendEmployeeDailyIncomeEmail() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        try {
            sendEmailService.sendEmployeeDailyIncomeEmail(
                    "recipient@test.com",
                    "Your Daily Earnings",
                    80.0,
                    20.0,
                    100.0,
                    LocalDate.now()
            );
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException(e);
        }

        verify(javaMailSender).send(mimeMessage);
    }
}

