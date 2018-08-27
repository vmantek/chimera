package com.vmantek.chimera.health;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name="org.springframework.boot.actuate.health.AbstractHealthIndicator")
public class Q2HealhConfiguration
{
    @Bean
    public Q2DeploymentsHealthIndicator q2HealthIndicator()
    {
        return new Q2DeploymentsHealthIndicator();
    }
}
