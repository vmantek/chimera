package com.vmantek.chimera;

import com.vmantek.chimera.q2.Q2Mods;
import com.vmantek.chimera.q2.SpringHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;

public class JPosApplication
{
    private static Logger log = LoggerFactory.getLogger(JPosApplication.class);

    public static void ensureDirsExists(String... dirs)
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

    public static void run(Class cls, final String[] args)
    {
        SpringApplication app = new SpringApplication(cls,JPosApplication.class);
        app.addInitializers(
            (ApplicationContextInitializer<ConfigurableApplicationContext>)
                applicationContext -> {
                    SpringHolder.setApplicationContext(applicationContext);
                    SpringHolder.setArgs(args);
                    Q2Mods.patchQ2();
                });
        app.run(args);
    }
}
