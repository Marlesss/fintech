package com.academy.fintech.origination.configuration;

import com.academy.fintech.origination.core.agreement.client.grpc.AgreementGrpcClientProperty;
import com.academy.fintech.origination.core.disbursement.client.grpc.DisbursementGrpcClientProperty;
import com.academy.fintech.origination.core.email.MailProperties;
import com.academy.fintech.origination.core.scoring.client.grpc.ScoringGrpcClientProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({AgreementGrpcClientProperty.class, ScoringGrpcClientProperty.class, DisbursementGrpcClientProperty.class, MailProperties.class})
public class OriginationConfiguration {
}
