package com.vmantek.chimera.q2;

import com.vmantek.chimera.deploy.SpringResourceDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;

@Configuration
public class Q2Configuration
{
    @Value("${q2.default-arguments:-r}")
    private String defaultArguments;

    public String getDefaultArguments()
    {
        return defaultArguments;
    }

    @Autowired
    private ApplicationArguments args;

    public void setDefaultArguments(String defaultArguments)
    {
        this.defaultArguments = defaultArguments;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SpringResourceDeployer sysDeployer(ConfigurableEnvironment ce)
    {
        return new SpringResourceDeployer(ce,"sys");
    }

    @Bean
    public SpringContextHolder springContextHolder()
    {
        return new SpringContextHolder();
    }

    @ConditionalOnMissingBean(Q2Service.class)
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Q2Service q2Service(SpringResourceDeployer sysDeployer)
    {
        File outputBase = sysDeployer.getOutputBase();
        return new Q2Service(outputBase.getAbsolutePath(),
                             defaultArguments,
                             args.getSourceArgs());
    }
}
