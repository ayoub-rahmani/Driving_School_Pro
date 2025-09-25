package org.example.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {
    private String senderEmail;
    private String senderPassword;
    private String smtpHost;
    private String smtpPort;
    private boolean configLoaded = false;

    /**
     * Initialize EmailService with properties from file or default values
     */
    public EmailService() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            if (input != null) {
                properties.load(input);
                this.senderEmail = properties.getProperty("email.sender");
                this.senderPassword = properties.getProperty("email.password");
                this.smtpHost = properties.getProperty("email.smtp.host");
                this.smtpPort = properties.getProperty("email.smtp.port");
                configLoaded = true;
            } else {
                // Use default values if properties file is not found
                System.out.println("email.properties file not found, using default values");
                this.senderEmail = "your_email@gmail.com";
                this.senderPassword = "your_app_password";
                this.smtpHost = "smtp.gmail.com";
                this.smtpPort = "587";
            }
        } catch (IOException e) {
            System.err.println("Error loading email.properties: " + e.getMessage());
            // Use default values if properties file cannot be loaded
            this.senderEmail = "your_email@gmail.com";
            this.senderPassword = "your_app_password";
            this.smtpHost = "smtp.gmail.com";
            this.smtpPort = "587";
        }
    }

    /**
     * Check if the service is properly configured with real credentials
     */
    public boolean isConfigured() {
        return configLoaded &&
                senderEmail != null && !senderEmail.isEmpty() && !senderEmail.equals("your_email@gmail.com") &&
                senderPassword != null && !senderPassword.isEmpty() && !senderPassword.equals("your_app_password");
    }

    /**
     * Send a verification code to the specified email address
     *
     * @param email The recipient's email address
     * @param code The verification code to send
     * @return true if the message was sent successfully, false otherwise
     */
    public boolean sendVerificationCode(String email, String code) {
        String subject = "Code de vérification Auto-École";
        String message = "Votre code de vérification Auto-École est: " + code;
        return sendEmail(email, subject, message);
    }

    /**
     * Send an email to the specified address
     *
     * @param toEmail The recipient's email address
     * @param subject The email subject
     * @param messageText The email body
     * @return true if the email was sent successfully, false otherwise
     */
    public boolean sendEmail(String toEmail, String subject, String messageText) {
        // If not properly configured, simulate success but log a warning
        if (!isConfigured()) {
            System.out.println("WARNING: Email service not properly configured. " +
                    "Simulating successful email delivery to: " + toEmail +
                    " with subject: " + subject +
                    " and message: " + messageText);
            return true;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(messageText);

            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
