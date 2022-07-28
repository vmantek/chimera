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
    @Override
    public void initialize(ConfigurableApplicationContext ctx)
    {
        SpringContextHolder.setContext(ctx);
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
}
