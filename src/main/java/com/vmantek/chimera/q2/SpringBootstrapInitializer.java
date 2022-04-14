package com.vmantek.chimera.q2;

import org.jpos.util.NameRegistrar;
import org.jpos.util.NameRegistrar.NotFoundException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.util.ArrayList;

public class SpringBootstrapInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>
{
    private static final String SPRING_APPCTX = "SPRING_APPCTX";

    @Override
    public void initialize(ConfigurableApplicationContext ctx)
    {
        NameRegistrar.register(SPRING_APPCTX, ctx);
        System.setProperty("javax.management.builder.initial", CustomMBeanServerBuilder.class.getName());

        MBeanServer server;

        ArrayList<MBeanServer> mbeanServerList = new ArrayList<>();
        do
        {
            mbeanServerList =
               MBeanServerFactory.findMBeanServer(null);
            if (!mbeanServerList.isEmpty())
            {
                server = (MBeanServer) mbeanServerList.get(0);
                MBeanServerFactory.releaseMBeanServer(server);
            }
        }
        while(!mbeanServerList.isEmpty());
    }

    public static ApplicationContext getApplicationContext()
    {
        try
        {
            return NameRegistrar.get(SPRING_APPCTX);
        }
        catch (NotFoundException e)
        {
            return null;
        }
    }
}
