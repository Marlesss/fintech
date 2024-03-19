package com.academy.fintech.origination.core.email;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mail.smtp")
public record MailProperties(String host, int port, String user, String password, String from) {
}
