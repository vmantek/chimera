package com.vmantek.chimera.q2;

import com.vmantek.chimera.deploy.SpringResourceDeployer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.util.Map;

@Configuration
public class Q2Configuration
{
    private final ApplicationArguments args;
    private final ApplicationContext ctx;

    @Value("${q2.default-arguments:-r}")
    private String defaultArguments;

    public Q2Configuration(
        ApplicationArguments args,
        ApplicationContext ctx)
    {
        this.args = args;
        this.ctx = ctx;
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
        boolean enableDeployer=false;
        String dir = "deploy";
        Map<String, Object> beans = ctx.getBeansWithAnnotation(EnableQ2.class);
        Object value = beans.values().stream()
            .findFirst()
            .orElseThrow(()->new IllegalArgumentException("@Q2Service annotation not found"));
        EnableQ2 eq2 = value.getClass().getAnnotation(EnableQ2.class);
        enableDeployer = eq2.enableDeployer();
        File deployDir = enableDeployer ? sysDeployer.getOutputBase() : new File(dir);
        return new Q2Service(deployDir.getAbsolutePath(),
                             defaultArguments,
                             args.getSourceArgs());
    }
}
