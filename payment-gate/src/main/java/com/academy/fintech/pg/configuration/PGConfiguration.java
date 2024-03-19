package com.academy.fintech.pg.configuration;

import com.academy.fintech.pg.core.agreement.client.grpc.AgreementGrpcClientProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AgreementGrpcClientProperty.class})
public class PGConfiguration {
}
