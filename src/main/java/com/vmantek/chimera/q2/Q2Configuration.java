package com.vmantek.chimera.q2;

import com.vmantek.chimera.components.Q2Properties;
import com.vmantek.chimera.db.JPosDatabaseAutoConfiguration;
import com.vmantek.chimera.deployment.SysDeployer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({JPosDatabaseAutoConfiguration.class})
@ConditionalOnProperty(prefix = "q2", name = "enabled", matchIfMissing = true)
public class Q2Configuration
{
    @Bean(initMethod = "start", destroyMethod = "stop")
    public SysDeployer sysDeployer()
    {
        return new SysDeployer();
    }

    @ConditionalOnMissingBean(Q2Service.class)
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Q2Service q2Service(SysDeployer sysDeployer, Q2Properties props)
    {
        return new Q2Service(sysDeployer.getBaseDir(),props);
    }
}
