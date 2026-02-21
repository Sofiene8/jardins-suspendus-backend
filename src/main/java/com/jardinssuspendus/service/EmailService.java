package com.jardinssuspendus.service;

import com.jardinssuspendus.entity.Reservation;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.sender-name}")
    private String senderName;

    /**
     * Envoie un email avec le code OTP
     */
    public void sendOTPEmail(String toEmail, String otpCode) {
        try {
            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            context.setVariable("appName", senderName);

            String htmlContent = templateEngine.process("otp-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject("Code de vérification - " + senderName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email OTP", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Envoie un email de confirmation de réservation
     */
    public void sendReservationConfirmationEmail(Reservation reservation) {
        try {
            Context context = new Context();
            context.setVariable("customerName", reservation.getUser().getName());
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("roomTitle", reservation.getRoom().getTitle());
            context.setVariable("startDate", reservation.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("endDate", reservation.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("adults", reservation.getAdults());
            context.setVariable("children6To12", reservation.getChildren6To12());
            context.setVariable("children0To5", reservation.getChildren0To5());
            context.setVariable("totalPrice", reservation.getTotalPrice());
            context.setVariable("nights", reservation.getNights());
            context.setVariable("appName", senderName);

            String htmlContent = templateEngine.process("reservation-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(reservation.getUser().getEmail());
            helper.setSubject("Confirmation de réservation #" + reservation.getId() + " - " + senderName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email de confirmation", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }

    /**
     * Envoie un email d'annulation de réservation
     */
    public void sendCancellationEmail(Reservation reservation) {
        try {
            Context context = new Context();
            context.setVariable("customerName", reservation.getUser().getName());
            context.setVariable("reservationId", reservation.getId());
            context.setVariable("roomTitle", reservation.getRoom().getTitle());
            context.setVariable("startDate", reservation.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("endDate", reservation.getEndDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("appName", senderName);

            String htmlContent = templateEngine.process("cancellation-email", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(reservation.getUser().getEmail());
            helper.setSubject("Annulation de réservation #" + reservation.getId() + " - " + senderName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email d'annulation", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }
    }
}