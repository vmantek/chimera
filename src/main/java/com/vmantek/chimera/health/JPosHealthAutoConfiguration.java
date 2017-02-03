package com.vmantek.chimera.health;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(HealthIndicator.class)
public class JPosHealthAutoConfiguration
{
    @Bean
    public Q2DeploymentsHealthIndicator jposHealthIndicator()
    {
        return new Q2DeploymentsHealthIndicator();
    }
}
