package com.vmantek.chimera;

import com.vmantek.chimera.deployment.SysDeployer;
import com.vmantek.chimera.q2.Q2Mods;
import com.vmantek.chimera.q2.Q2Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;

public class JPosApplication
{
    private static Logger log = LoggerFactory.getLogger(JPosApplication.class);

    public JPosApplication()
    {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("jdk.tls.ephemeralDHKeySize", "2048");
        System.setProperty("jdk.tls.rejectClientInitiatedRenegotiation", "true");
        System.setProperty("jsse.enableCBCProtection", "true");
        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "false");
        System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "false");
        ensureDirsExists("./log", "./db", "./cfg");
    }

    protected static void run(Class cls, String[] args)
    {
        Q2Mods.patchQ2();

        System.setProperty("spring.config.location", "file:cfg/");
        SpringApplication app = new SpringApplication(JPosApplication.class, cls);
        app.setBannerMode(Mode.OFF);
        app.run(args);
    }

    private void ensureDirsExists(String... dirs)
    {
        for (String dir : dirs)
        {
            File d = new File(dir);
            if (!d.exists())
            {
                if (!d.mkdirs())
                {
                    log.warn("Could not create directory: " + dir);
                }
            }
        }
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public SysDeployer sysDeployer()
    {
        return new SysDeployer();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Q2Service getQ2(SysDeployer sysDeployer)
    {
        return new Q2Service(sysDeployer.getBaseDir());
    }
}
