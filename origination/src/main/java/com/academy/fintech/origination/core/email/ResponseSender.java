package com.academy.fintech.origination.core.email;

import com.academy.fintech.application.ClientData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Slf4j
@Service
public class ResponseSender {
    MailProperties mailProperties;
    private Session session;

    ResponseSender(MailProperties mailProperties) {
        this.mailProperties = mailProperties;
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtps");
        properties.put("mail.smtp.host", mailProperties.host());
        properties.put("mail.smtp.port", mailProperties.port());
        properties.put("mail.smtp.user", mailProperties.user());
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        mailProperties.from(),
                        mailProperties.password()
                );
            }
        };
        session = Session.getInstance(properties, authenticator);
    }

    private MimeMessage getMessage(String toEmailAddress) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(mailProperties.from());
        message.setRecipients(Message.RecipientType.TO, toEmailAddress);
        return message;
    }

    public void sendWithHello(ClientData clientData, String subject, String text) {
        try {
            MimeMessage message = getMessage(clientData.getEmail());
            message.setSubject(subject);
            message.setText(String.format("Здравствуйте, %s %s! %s",
                    clientData.getFirstName(), clientData.getLastName(), text));
            Transport.send(message);
        } catch (RuntimeException | MessagingException e) {
            log.error("Failed to send message", e);
        }
    }

    public void sendRejected(ClientData clientData) {
        sendWithHello(clientData, "Заявка на кредит была отклонена", "К сожалению, вынуждены сообщить, что" +
                " мы не можем сейчас одобрить вам кредит. Вы можете обратиться в офис нашего банка для выяснения причины отклонения заявки.");
    }

    public void sendAccepted(ClientData clientData) {
        sendWithHello(clientData, "Заявка на кредит одобрена", "Ваша заявка на кредит была одобрена! " +
                "Ожидайте поступления средств на ваш счёт в ближайшее время.");
    }
}
