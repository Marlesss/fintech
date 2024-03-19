package com.academy.fintech.origination.core.disbursement.client.grpc;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "origination.client.disbursement.grpc")
public record DisbursementGrpcClientProperty(String host, int port) {
}
