package com.cotato.itda.domain.sms.infra.solapi.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SolapiProperties.class)
public class SolapiPropertiesConfig {
}
