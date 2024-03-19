package com.academy.fintech.pg.core.agreement.client.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "disbursement.client.agreement.grpc")
public record AgreementGrpcClientProperty(String host, int port) {
}
