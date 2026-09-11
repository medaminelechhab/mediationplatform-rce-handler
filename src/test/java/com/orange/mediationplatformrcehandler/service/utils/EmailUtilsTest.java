package com.MyProject.mediationplatformrcehandler.service.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class EmailUtilsTest {

    private EmailUtils emailUtils;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage message;

    @BeforeEach
    void before() {
        emailUtils = new EmailUtils(javaMailSender);
    }

    @Test
    @DisplayName("Verify send of a simple email without attachments")
    void givenAListOfExceptions_WhenSendIsCalledAnEmailSent() {
        ArgumentCaptor<SimpleMailMessage> simpleMailMessageArgumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        doNothing().when(javaMailSender).send(message);

        emailUtils.sendSimpleMessage(List.of(new Exception()));

        verify(javaMailSender, times(1)).send(simpleMailMessageArgumentCaptor.capture());
        Assertions.assertNull(simpleMailMessageArgumentCaptor.getValue().getSubject());
    }

    @Test
    @DisplayName("Verify send of an email with attachment")
    void givenAFilePath_WhenSendIsCalledAnEmailSentWithAttachment() throws MessagingException {
        ArgumentCaptor<MimeMessage> mimeMessageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        when(javaMailSender.createMimeMessage()).thenReturn(message);

        ReflectionTestUtils.setField(emailUtils, "temporaryDir", "");
        ReflectionTestUtils.setField(emailUtils, "clinksRecipients", new String[0]);
        ReflectionTestUtils.setField(emailUtils, "contentErrorSubject", "");
        ReflectionTestUtils.setField(emailUtils, "contentErrorBody", "");

        doNothing().when(javaMailSender).send(message);

        emailUtils.sendMailWithAttachment(true, "test.csv");

        verify(javaMailSender).send(mimeMessageArgumentCaptor.capture());
        Assertions.assertNull(mimeMessageArgumentCaptor.getValue().getSubject());
    }
}
