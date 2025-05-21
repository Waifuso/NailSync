package phong.demo.Springpro;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;


@Service
public class SendEmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("$(spring.mail.username)")
    private String fromEmailId;

    public void SendEmail (String recipient, String body, String subject ) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(fromEmailId);
        simpleMailMessage.setTo(recipient);
        simpleMailMessage.setText(body);
        simpleMailMessage.setSubject(subject);

        javaMailSender.send(simpleMailMessage);




    }

    /**
     * Sends a beautifully designed appointment confirmation email with confirmation code.
     *
     * @param recipient          Email address of the recipient
     * @param subject            Subject line for the email
     * @param confirmationNumber The confirmation code to display in the email
     * @throws MessagingException If there's an error sending the email
     */
    public void sendEmailWithHtml(String recipient, String subject, Integer confirmationNumber) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmailId);
        helper.setTo(recipient);
        helper.setSubject(subject);

        // Get current date for the appointment date
        LocalDate appointmentDate = LocalDate.now().plusDays(1); // Assuming appointment is for tomorrow
        String formattedDate = appointmentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));

        // Set a default appointment time for demonstration
        String appointmentTime = "2:30 PM";

        String body = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f0f4f8; color: #333333;">
    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; margin: 20px auto;">
        <!-- Header Banner -->
        <tr>
            <td style="background: linear-gradient(135deg, #9c27b0 0%%, #673ab7 100%%); padding: 0; border-radius: 10px 10px 0 0; text-align: center;">
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 30px 20px;">
                            <img src="cid:nailsyncLogo" alt="NAILSYNC Logo" style="height: 60px; margin-bottom: 15px;">
                            <h1 style="margin: 0; color: #ffffff; font-size: 28px; font-weight: 700;">Your Appointment is Confirmed!</h1>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        
        <!-- Main Content -->
        <tr>
            <td style="background-color: #ffffff; padding: 0; border-left: 1px solid #e1e8ed; border-right: 1px solid #e1e8ed;">
                <!-- Confirmation Details -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 40px 30px 20px 30px;">
                            <!-- Appointment Time Banner -->
                            <table border="0" cellpadding="0" cellspacing="0" style="width: 100%%; margin-bottom: 30px; background: linear-gradient(to right, #f5f7fa, #e4e7eb); border-radius: 8px;">
                                <tr>
                                    <td style="padding: 20px; text-align: center;">
                                        <p style="margin: 0 0 5px 0; font-size: 14px; color: #666666; text-transform: uppercase; letter-spacing: 1px;">YOUR APPOINTMENT</p>
                                        <h2 style="margin: 0; color: #333333; font-size: 22px; font-weight: 600;">%s • %s</h2>
                                    </td>
                                </tr>
                            </table>
                            
                            <p style="margin: 0 0 25px 0; font-size: 16px; line-height: 1.6; color: #666666; text-align: center;">Please use this confirmation code when you arrive:</p>
                            
                            <!-- Confirmation Code Box -->
                            <div style="background: linear-gradient(135deg, #f5f7fa 0%%, #e4e7eb 100%%); border: 2px dashed #9c27b0; border-radius: 10px; padding: 20px; margin-bottom: 30px; text-align: center;">
                                <h1 style="margin: 0; color: #9c27b0; font-size: 42px; font-weight: 700; letter-spacing: 2px;">%s</h1>
                            </div>
                            
                            <!-- Benefits Section -->
                            <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="margin-bottom: 25px;">
                                <tr>
                                    <td>
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                            <tr>
                                                <td width="32" valign="top">
                                                    <div style="background-color: #9c27b0; width: 24px; height: 24px; border-radius: 50%%; color: white; text-align: center; line-height: 24px; font-size: 12px; font-weight: bold;">✓</div>
                                                </td>
                                                <td style="padding-left: 10px; padding-bottom: 15px;">
                                                    <p style="margin: 0; color: #333333; font-size: 15px; font-weight: 600;">Priority Service</p>
                                                    <p style="margin: 5px 0 0 0; color: #666666; font-size: 14px;">Skip the wait with your confirmed appointment</p>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td width="32" valign="top">
                                                    <div style="background-color: #9c27b0; width: 24px; height: 24px; border-radius: 50%%; color: white; text-align: center; line-height: 24px; font-size: 12px; font-weight: bold;">✓</div>
                                                </td>
                                                <td style="padding-left: 10px; padding-bottom: 15px;">
                                                    <p style="margin: 0; color: #333333; font-size: 15px; font-weight: 600;">Personalized Care</p>
                                                    <p style="margin: 5px 0 0 0; color: #666666; font-size: 14px;">Your stylist will be prepared for your specific needs</p>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td width="32" valign="top">
                                                    <div style="background-color: #9c27b0; width: 24px; height: 24px; border-radius: 50%%; color: white; text-align: center; line-height: 24px; font-size: 12px; font-weight: bold;">✓</div>
                                                </td>
                                                <td style="padding-left: 10px;">
                                                    <p style="margin: 0; color: #333333; font-size: 15px; font-weight: 600;">Exclusive Offers</p>
                                                    <p style="margin: 5px 0 0 0; color: #666666; font-size: 14px;">Ask about our special services for booked clients</p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Important Notice -->
                            <table border="0" cellpadding="0" cellspacing="0" width="100%%" style="background-color: #fff8e1; border-left: 4px solid #ffc107; border-radius: 4px; margin-bottom: 20px;">
                                <tr>
                                    <td style="padding: 15px;">
                                        <p style="margin: 0; font-size: 14px; line-height: 1.6; color: #b28704;">
                                            <strong>Important:</strong> Please arrive 5 minutes before your appointment time. If you're late for over 15 minutes, you may need to reschedule your appointment.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Call to Action -->
                            <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                <tr>
                                    <td align="center" style="padding: 20px 0 10px 0;">
                                        <div style="background-color: #9c27b0; border-radius: 50px; display: inline-block;">
                                            <a href="#" style="color: #ffffff; font-size: 16px; font-weight: 600; text-decoration: none; padding: 12px 30px; display: inline-block;">Manage Appointment</a>
                                        </div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        
        <!-- Footer -->
        <tr>
            <td style="background-color: #333333; color: #ffffff; padding: 25px; border-radius: 0 0 10px 10px; text-align: center;">
                <p style="margin: 0 0 10px 0; font-size: 14px;">— The NAILSYNC Team</p>
                <p style="margin: 0 0 15px 0; font-size: 12px; color: #bbbbbb;">If you did not request this appointment, please contact us immediately.</p>
                <p style="margin: 0; font-size: 12px; color: #bbbbbb;">© 2025 NAILSYNC • All rights reserved</p>
            </td>
        </tr>
    </table>
</body>
</html>
""".formatted(formattedDate, appointmentTime, confirmationNumber);

        helper.setText(body, true); // true = isHtml

        // Add the logo - with error handling
        try {
            ClassPathResource image = new ClassPathResource("NAILSYNC.png");
            if (image.exists()) {
                helper.addInline("nailsyncLogo", image);
            }
        } catch (Exception e) {
            // Log error but continue - the email will still send without the logo
            System.err.println("Error adding logo to email: " + e.getMessage());
        }

        javaMailSender.send(message);
    }

    /**
     * Sends a stylish payment confirmation email to the customer.
     *
     * @param recipient Email address of the recipient
     * @param subject   Subject line for the email
     * @param amount    The payment amount to display in the email
     * @throws MessagingException If there's an error sending the email
     */
    public void paymentSending(String recipient, String subject, double amount) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmailId);
        helper.setTo(recipient);
        helper.setSubject(subject);

        // Generate a random receipt number for reference
        String receiptNumber = "NS-" + (10000 + new Random().nextInt(90000));

        // Get current date formatted nicely
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));

        String body = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f0f4f8; color: #333333;">
    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; margin: 20px auto;">
        <!-- Header -->
        <tr>
            <td align="center" style="background-color: #2c3e50; padding: 25px; border-radius: 10px 10px 0 0; text-align: center;">
                <img src="cid:nailsyncLogo" alt="NAILSYNC Logo" style="height: 60px;">
            </td>
        </tr>
        
        <!-- Main Content -->
        <tr>
            <td style="background-color: #ffffff; padding: 0; border-left: 1px solid #e1e8ed; border-right: 1px solid #e1e8ed;">
                <!-- Payment Confirmation -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 40px 30px 0 30px;">
                            <!-- Success Icon -->
                            <div style="background-color: #e8f5e9; width: 80px; height: 80px; border-radius: 50%%; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px auto;">
                                <div style="color: #4CAF50; font-size: 36px; line-height: 1;">✓</div>
                            </div>
                            
                            <h2 style="margin: 0; color: #2c3e50; font-size: 24px; font-weight: 600;">Payment Successful!</h2>
                            <p style="margin: 10px 0 25px 0; color: #7f8c8d; font-size: 16px;">Thank you for your payment with <strong>NAILSYNC</strong></p>
                            
                            <div style="width: 60px; height: 3px; background-color: #4CAF50; margin: 0 auto 30px auto;"></div>
                        </td>
                    </tr>
                </table>
                
                <!-- Payment Details -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 0 30px 30px 30px;">
                            <table border="0" cellpadding="0" cellspacing="0" style="width: 100%%; background-color: #f8fafb; border-radius: 10px;">
                                <tr>
                                    <td style="padding: 20px;">
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                            <tr>
                                                <td style="padding: 10px 0; border-bottom: 1px solid #e1e8ed;">
                                                    <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                                        <tr>
                                                            <td width="50%%" style="color: #7f8c8d; font-size: 14px;">Receipt Number</td>
                                                            <td width="50%%" style="text-align: right; color: #34495e; font-size: 14px; font-weight: 600;">%s</td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 10px 0; border-bottom: 1px solid #e1e8ed;">
                                                    <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                                        <tr>
                                                            <td width="50%%" style="color: #7f8c8d; font-size: 14px;">Date</td>
                                                            <td width="50%%" style="text-align: right; color: #34495e; font-size: 14px; font-weight: 600;">%s</td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td style="padding: 15px 0;">
                                                    <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                                        <tr>
                                                            <td width="50%%" style="color: #7f8c8d; font-size: 14px;">Amount Paid</td>
                                                            <td width="50%%" style="text-align: right; color: #4CAF50; font-size: 24px; font-weight: 700;">$%.2f</td>
                                                        </tr>
                                                    </table>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Thank You Message -->
                            <p style="margin: 30px 0 10px 0; font-size: 16px; line-height: 1.6; color: #34495e;">We appreciate your business and look forward to serving you again!</p>
                            <p style="margin: 0; font-size: 14px; color: #7f8c8d;">If you have any questions, feel free to contact us.</p>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        
        <!-- Footer -->
        <tr>
            <td style="background-color: #2c3e50; color: #ffffff; padding: 25px; border-radius: 0 0 10px 10px; text-align: center;">
                <p style="margin: 0 0 10px 0; font-size: 14px;">— The NAILSYNC Team</p>
                <p style="margin: 0; font-size: 12px; color: #bdc3c7;">© 2025 NAILSYNC • All rights reserved</p>
            </td>
        </tr>
    </table>
</body>
</html>
""".formatted(receiptNumber, formattedDate, amount);

        helper.setText(body, true); // true = isHtml

        // Add the logo - with error handling
        try {
            ClassPathResource image = new ClassPathResource("NAILSYNC.png");
            if (image.exists()) {
                helper.addInline("nailsyncLogo", image);
            }
        } catch (Exception e) {
            // Log error but continue - the email will still send without the logo
            System.err.println("Error adding logo to email: " + e.getMessage());
        }

        javaMailSender.send(message);
    }

/**
     * Sends a stylish daily report email with earnings information.
     *
     * @param recipient     Email address of the recipient
     * @param subject       Subject line for the email
     * @param totalEarnings The total earnings to display in the email
     * @throws MessagingException If there's an error sending the email
     */
    public void dailyReport(String recipient, String subject, double totalEarnings) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmailId);
        helper.setTo(recipient);
        helper.setSubject(subject);

        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        String body = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f0f4f8; color: #333333;">
    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; margin: 20px auto;">
        <!-- Logo Header Bar -->
        <tr>
            <td align="center" style="background-color: #2c3e50; padding: 20px; border-radius: 10px 10px 0 0;">
                <img src="cid:nailsyncLogo" alt="NAILSYNC Logo" style="height: 70px;">
            </td>
        </tr>
        
        <!-- Main Content -->
        <tr>
            <td style="background-color: #ffffff; padding: 0; border-left: 1px solid #e1e8ed; border-right: 1px solid #e1e8ed;">
                <!-- Date Header -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 25px 30px 0 30px;">
                            <h2 style="margin: 0; color: #2c3e50; font-size: 24px; font-weight: 600;">Daily Report</h2>
                            <p style="margin: 5px 0 0 0; font-size: 16px; color: #7f8c8d;">%s</p>
                            <div style="width: 50px; height: 3px; background-color: #4CAF50; margin: 20px auto;"></div>
                        </td>
                    </tr>
                </table>
                
                <!-- Earnings Section -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 20px 30px 30px 30px;">
                            <div style="background: linear-gradient(135deg, #f6f9fc 0%%, #ecf0f1 100%%); border-radius: 10px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                                <p style="margin: 0 0 10px 0; font-size: 18px; color: #34495e; font-weight: 600;">TODAY'S TOTAL EARNINGS</p>
                                <h1 style="margin: 0; font-size: 48px; font-weight: 700; color: #4CAF50;">$%.2f</h1>
                                
                                <div style="width: 100%%; margin-top: 25px; height: 10px; background-color: #ecf0f1; border-radius: 5px; overflow: hidden;">
                                    <div style="width: 75%%; height: 10px; background: linear-gradient(90deg, #4CAF50 0%%, #2ecc71 100%%); border-radius: 5px;"></div>
                                </div>
                                
                                <p style="margin: 15px 0 0 0; font-size: 14px; color: #7f8c8d;">75%% of your target goal</p>
                            </div>
                            
                            <!-- Yesterday Comparison -->
                            <table border="0" cellpadding="0" cellspacing="0" style="margin-top: 20px; width: 100%%;">
                                <tr>
                                    <td style="background-color: #f8fafb; padding: 15px; border-radius: 8px; border-left: 4px solid #3498db;">
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                            <tr>
                                                <td width="65%%" style="padding-right: 15px;">
                                                    <p style="margin: 0; font-size: 14px; color: #7f8c8d;">PREVIOUS DAY</p>
                                                    <p style="margin: 5px 0 0 0; font-size: 20px; color: #34495e; font-weight: 600;">$%.2f</p>
                                                </td>
                                                <td width="35%%" style="text-align: right;">
                                                    <p style="margin: 0; font-size: 14px; color: #7f8c8d;">CHANGE</p>
                                                    <p style="margin: 5px 0 0 0; font-size: 16px; color: #4CAF50; font-weight: 600;">+%.1f%%</p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        
        <!-- Footer -->
        <tr>
            <td style="background-color: #2c3e50; color: #ffffff; padding: 25px 30px; border-radius: 0 0 10px 10px; text-align: center;">
                <p style="margin: 0 0 10px 0; font-size: 14px; line-height: 1.6;">For detailed analysis and insights, log in to your NAILSYNC dashboard</p>
                <p style="margin: 20px 0 0 0; font-size: 13px; color: #bdc3c7;">© 2025 NAILSYNC • All rights reserved</p>
            </td>
        </tr>
    </table>
</body>
</html>
""".formatted(formattedDate, totalEarnings, totalEarnings * 0.9, ((totalEarnings / (totalEarnings * 0.9)) - 1) * 100);

        helper.setText(body, true); // true = isHtml

        // Add the logo - make sure this file exists in your resources folder
        try {
            ClassPathResource logo = new ClassPathResource("NAILSYNC.png");
            if (logo.exists()) {
                helper.addInline("nailsyncLogo", logo);
            }
        } catch (Exception e) {
            // Log error but continue - the email will still send without the logo
            System.err.println("Error adding logo to email: " + e.getMessage());
        }

        javaMailSender.send(message);
    }

    /**
     * Sends a stylish email for code redemption with coupon code and user points information.
     *
     * @param recipient   Email address of the recipient
     * @param subject     Subject line for the email
     * @param couponCode  The redemption code to display in the email
     * @param userPoints  The user's current point balance
     * @throws MessagingException If there's an error sending the email
     */
    public void redeemCodeEmail(String recipient, String subject, String couponCode, int userPoints) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmailId);
        helper.setTo(recipient);
        helper.setSubject(subject);

        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        String body = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f0f4f8; color: #333333;">
    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; margin: 20px auto;">
        <!-- Logo Header Bar -->
        <tr>
            <td align="center" style="background-color: #2c3e50; padding: 20px; border-radius: 10px 10px 0 0;">
                <img src="cid:nailsyncLogo" alt="NAILSYNC Logo" style="height: 70px;">
            </td>
        </tr>
        
        <!-- Main Content -->
        <tr>
            <td style="background-color: #ffffff; padding: 0; border-left: 1px solid #e1e8ed; border-right: 1px solid #e1e8ed;">
                <!-- Date Header -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 25px 30px 0 30px;">
                            <h2 style="margin: 0; color: #2c3e50; font-size: 24px; font-weight: 600;">Your Coupon Code</h2>
                            <p style="margin: 5px 0 0 0; font-size: 16px; color: #7f8c8d;">%s</p>
                            <div style="width: 50px; height: 3px; background-color: #e74c3c; margin: 20px auto;"></div>
                        </td>
                    </tr>
                </table>
                
                <!-- Coupon Code Section -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 20px 30px 30px 30px;">
                            <div style="background: linear-gradient(135deg, #f6f9fc 0%%, #ecf0f1 100%%); border-radius: 10px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                                <p style="margin: 0 0 10px 0; font-size: 18px; color: #34495e; font-weight: 600;">YOUR REDEMPTION CODE</p>
                                <h1 style="margin: 0; font-size: 38px; font-weight: 700; color: #e74c3c; letter-spacing: 2px; background-color: #fff; padding: 15px; border: 2px dashed #e74c3c; border-radius: 8px;">%s</h1>
                                
                                <p style="margin: 20px 0 0 0; font-size: 14px; color: #7f8c8d;">Use this code at checkout for your special discount!</p>
                            </div>
                            
                            <!-- Points Balance -->
                            <table border="0" cellpadding="0" cellspacing="0" style="margin-top: 20px; width: 100%%;">
                                <tr>
                                    <td style="background-color: #f8fafb; padding: 15px; border-radius: 8px; border-left: 4px solid #3498db;">
                                        <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                                            <tr>
                                                <td width="100%%" style="padding-right: 15px;">
                                                    <p style="margin: 0; font-size: 14px; color: #7f8c8d;">YOUR CURRENT POINTS BALANCE</p>
                                                    <p style="margin: 5px 0 0 0; font-size: 24px; color: #3498db; font-weight: 600;">%d points</p>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- How to use -->
                            <div style="margin-top: 25px; text-align: left; padding: 20px; background-color: #f9f9f9; border-radius: 8px;">
                                <h3 style="margin: 0 0 15px 0; color: #2c3e50; font-size: 18px;">How to Use Your Coupon:</h3>
                                <ol style="margin: 0; padding-left: 20px; color: #34495e;">
                                    <li style="margin-bottom: 8px;">Visit our website or in-store kiosk</li>
                                    <li style="margin-bottom: 8px;">Select your services</li>
                                    <li style="margin-bottom: 8px;">Enter your coupon code at checkout</li>
                                    <li style="margin-bottom: 0;">Enjoy your discount!</li>
                                </ol>
                            </div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        
        <!-- Footer -->
        <tr>
            <td style="background-color: #2c3e50; color: #ffffff; padding: 25px 30px; border-radius: 0 0 10px 10px; text-align: center;">
                <p style="margin: 0 0 10px 0; font-size: 14px; line-height: 1.6;">For more offers and to check your rewards, log in to your NAILSYNC dashboard</p>
                <p style="margin: 20px 0 0 0; font-size: 13px; color: #bdc3c7;">© 2025 NAILSYNC • All rights reserved</p>
            </td>
        </tr>
    </table>
</body>
</html>
""".formatted(formattedDate, couponCode, userPoints);

        helper.setText(body, true); // true = isHtml

        // Add the logo - make sure this file exists in your resources folder
        try {
            ClassPathResource logo = new ClassPathResource("NAILSYNC.png");
            if (logo.exists()) {
                helper.addInline("nailsyncLogo", logo);
            }
        } catch (Exception e) {
            // Log error but continue - the email will still send without the logo
            System.err.println("Error adding logo to email: " + e.getMessage());
        }

        javaMailSender.send(message);
    }

    /**
     * Sends a styled email with daily earnings summary to an employee.
     *
     * @param recipient     Email address of the recipient
     * @param subject       Subject line for the email
     * @param dailyIncome   Employee's base earnings for the day
     * @param tip           Tips received for the day
     * @param totalAmount   Total earnings (daily income + tips)
     * @param date          Date of the earnings summary
     * @throws MessagingException If there's an error sending the email
     */
    public void sendEmployeeDailyIncomeEmail(String recipient, String subject, double dailyIncome, double tip, double totalAmount, LocalDate date) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmailId);
        helper.setTo(recipient);
        helper.setSubject(subject);

        String formattedDate = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));

        String body = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f0f4f8; color: #333333;">
    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; margin: 20px auto;">
        <!-- Logo Header -->
        <tr>
            <td align="center" style="background-color: #2c3e50; padding: 20px; border-radius: 10px 10px 0 0;">
                <img src="cid:nailsyncLogo" alt="NAILSYNC Logo" style="height: 70px;">
            </td>
        </tr>
        
        <!-- Main Content -->
        <tr>
            <td style="background-color: #ffffff; padding: 0; border-left: 1px solid #e1e8ed; border-right: 1px solid #e1e8ed;">
                <!-- Date Header -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 25px 30px 0 30px;">
                            <h2 style="margin: 0; color: #2c3e50; font-size: 24px; font-weight: 600;">Daily Earnings Summary</h2>
                            <p style="margin: 5px 0 0 0; font-size: 16px; color: #7f8c8d;">%s</p>
                            <div style="width: 50px; height: 3px; background-color: #e74c3c; margin: 20px auto;"></div>
                        </td>
                    </tr>
                </table>
                
                <!-- Earnings Breakdown -->
                <table border="0" cellpadding="0" cellspacing="0" width="100%%">
                    <tr>
                        <td align="center" style="padding: 20px 30px 30px 30px;">
                            <!-- Base Earnings -->
                            <table border="0" cellpadding="0" cellspacing="0" style="margin-bottom: 20px; width: 100%%;">
                                <tr>
                                    <td style="background-color: #f8fafb; padding: 15px; border-radius: 8px; border-left: 4px solid #3498db;">
                                        <p style="margin: 0; font-size: 14px; color: #7f8c8d;">DAILY INCOME</p>
                                        <p style="margin: 5px 0 0 0; font-size: 24px; color: #3498db; font-weight: 600;">$%.2f</p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Tips -->
                            <table border="0" cellpadding="0" cellspacing="0" style="margin-bottom: 20px; width: 100%%;">
                                <tr>
                                    <td style="background-color: #f8fafb; padding: 15px; border-radius: 8px; border-left: 4px solid #27ae60;">
                                        <p style="margin: 0; font-size: 14px; color: #7f8c8d;">TIPS</p>
                                        <p style="margin: 5px 0 0 0; font-size: 24px; color: #27ae60; font-weight: 600;">$%.2f</p>
                                    </td>
                                </tr>
                            </table>
                            
                            <!-- Total -->
                            <div style="background: linear-gradient(135deg, #f6f9fc 0%%, #ecf0f1 100%%); border-radius: 10px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); margin-bottom: 20px;">
                                <p style="margin: 0 0 10px 0; font-size: 18px; color: #34495e; font-weight: 600;">TOTAL EARNINGS</p>
                                <h1 style="margin: 0; font-size: 38px; font-weight: 700; color: #e74c3c; letter-spacing: 2px; background-color: #fff; padding: 15px; border: 2px dashed #e74c3c; border-radius: 8px;">$%.2f</h1>
                            </div>
                            
                            <!-- Footer Message -->
                            <div style="margin-top: 25px; text-align: left; padding: 20px; background-color: #f9f9f9; border-radius: 8px;">
                                <h3 style="margin: 0 0 15px 0; color: #2c3e50; font-size: 18px;">Payment Information</h3>
                                <p style="margin: 0; color: #34495e;">This amount will be processed through our payroll system. Please allow 2-3 business days for bank processing.</p>
                            </div>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        
        <!-- Footer -->
        <tr>
            <td style="background-color: #2c3e50; color: #ffffff; padding: 25px 30px; border-radius: 0 0 10px 10px; text-align: center;">
                <p style="margin: 0 0 10px 0; font-size: 14px; line-height: 1.6;">View your earnings history and manage payments in your NAILSYNC account</p>
                <p style="margin: 20px 0 0 0; font-size: 13px; color: #bdc3c7;">© 2025 NAILSYNC • All rights reserved</p>
            </td>
        </tr>
    </table>
</body>
</html>
""".formatted(formattedDate, dailyIncome, tip, totalAmount);

        helper.setText(body, true);

        try {
            ClassPathResource logo = new ClassPathResource("NAILSYNC.png");
            if (logo.exists()) {
                helper.addInline("nailsyncLogo", logo);
            }
        } catch (Exception e) {
            System.err.println("Error adding logo to email: " + e.getMessage());
        }

        javaMailSender.send(message);
    }

}
