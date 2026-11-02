
package com.smartbook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@smartbook.local}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Appointment confirmation email
    public void sendAppointmentConfirmation(
            String customerEmail,
            String customerName,
            String serviceName,
            LocalDateTime appointmentDateTime) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(customerEmail);
        message.setSubject("Your appointment is confirmed - SmartBook");

        message.setText(
                "Hello " + customerName + ",\n\n"
                        + "Your appointment has been successfully booked.\n\n"
                        + "Service: " + serviceName + "\n"
                        + "Date and time: " + appointmentDateTime + "\n\n"
                        + "Thank you for choosing us!\n"
                        + "Powered by SmartBook"
        );

        mailSender.send(message);
    }

    // Appointment reminder email
    public void sendAppointmentReminder(
            String customerEmail,
            String customerName,
            String serviceName,
            LocalDateTime appointmentDateTime) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(customerEmail);
        message.setSubject("Appointment Reminder - SmartBook");

        message.setText(
                "Hello " + customerName + ",\n\n"
                        + "This is a reminder about your upcoming appointment.\n\n"
                        + "Service: " + serviceName + "\n"
                        + "Date and time: " + appointmentDateTime + "\n\n"
                        + "We look forward to seeing you!\n\n"
                        + "Powered by SmartBook"
        );

        mailSender.send(message);
    }
    // Password reset email
    public void sendPasswordResetEmail(
            String email,
            String resetUrl
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Reset your SmartBook password");

        message.setText(
                "Hello,\n\n"
                        + "We received a request to reset your SmartBook password.\n\n"
                        + "Use the link below to choose a new password:\n"
                        + resetUrl + "\n\n"
                        + "This link will expire in 30 minutes.\n"
                        + "If you did not request this, you can ignore this email.\n\n"
                        + "Powered by SmartBook"
        );

        mailSender.send(message);
    }

    // Email verification
    public void sendVerificationEmail(
            String email,
            String verificationUrl
    ) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Verify your SmartBook email");

        message.setText(
                "Welcome to SmartBook!\n\n"
                        + "Please verify your email address using this link:\n"
                        + verificationUrl + "\n\n"
                        + "If you did not create this account, you can ignore this email.\n\n"
                        + "Powered by SmartBook"
        );

        mailSender.send(message);
    }
}
