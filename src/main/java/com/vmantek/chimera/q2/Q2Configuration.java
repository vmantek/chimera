package com.vmantek.chimera.q2;

import com.vmantek.chimera.db.JPosDatabaseAutoConfiguration;
import com.vmantek.chimera.deployment.SysDeployer;
import com.vmantek.chimera.health.Q2DeploymentsHealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
public class Q2Configuration
{
    @Value("${q2.default-arguments:-r}")
    private String defaultArguments;

    public String getDefaultArguments()
    {
        return defaultArguments;
    }

    public void setDefaultArguments(String defaultArguments)
    {
        this.defaultArguments = defaultArguments;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SysDeployer sysDeployer()
    {
        return new SysDeployer();
    }

    @ConditionalOnMissingBean(Q2Service.class)
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Q2Service q2Service(SysDeployer sysDeployer)
    {
        return new Q2Service(sysDeployer.getBaseDir(),defaultArguments);
    }
}
