package com.MyProject.mediationplatformrcehandler.service.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailUtils {

    private static final String MSG_LOG_MAIL_EXCEPTION = "An error occurred while sending the email %s";

    private final JavaMailSender emailSender;

    @Value("${clinksplatform.customerlinks.rce.clinks-recipient}")
    private String[] clinksRecipients;
    @Value("${clinksplatform.customerlinks.rce.ptf-recipient}")
    private String[] ptfRecipients;
    @Value("${clinksplatform.customerlinks.rce.mail-sender}")
    private String sender;
    @Value("${clinksplatform.customerlinks.rce.mail-content-error-subject}")
    private String contentErrorSubject;
    @Value("${clinksplatform.customerlinks.rce.mail-content-error-body}")
    private String contentErrorBody;
    @Value("${clinksplatform.customerlinks.rce.mail-technical-error-subject}")
    private String technicalErrorSubject;
    @Value("${clinksplatform.customerlinks.rce.mail-technical-error-body}")
    private String technicalErrorBody;
    @Value("${clinksplatform.tmp-storage-dir}")
    private String temporaryDir;

    public void sendMailWithAttachment(boolean hasError, String... filenames) throws MessagingException {
        log.info("Sending email with attachement : {}", Arrays.asList(filenames));
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            message.setFrom(sender);
            helper.setTo(clinksRecipients);
            if (hasError) {
                helper.setSubject(contentErrorSubject);
                helper.setText(contentErrorBody);
            } else {
                helper.setSubject("[RCE] Import OK");
                helper.setText("Hello, RCE import was successful on " + LocalDateTime.now());
            }

            for (String attachement : filenames) {
                if(attachement!=null && !attachement.isEmpty()
                    && temporaryDir!=null && !temporaryDir.isEmpty()){
                    var file = new File(temporaryDir + attachement);
                    helper.addAttachment(file.getName(), file);
                }
            }

            emailSender.send(message);
            log.info("Email sent !");
        } catch (Exception mailException) {
            log.error(String.format(MSG_LOG_MAIL_EXCEPTION, mailException.getMessage()));
        }
    }

    public void sendSimpleMessage(List<Throwable> failures) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(ptfRecipients);
        message.setSubject(technicalErrorSubject);
        message.setText(technicalErrorBody + failures.toString());

        emailSender.send(message);
    }
}
