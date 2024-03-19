package com.academy.fintech.scoring.configuration;

import com.academy.fintech.scoring.core.agreement.client.grpc.AgreementGrpcClientProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AgreementGrpcClientProperty.class})
public class ScoringConfiguration {
}
