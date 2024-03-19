package com.academy.fintech.origination.core.agreement.client.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "origination.client.agreement.grpc")
public record AgreementGrpcClientProperty(String host, int port) {
}
