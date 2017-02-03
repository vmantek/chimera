package com.vmantek.chimera.q2;

import org.springframework.context.ApplicationContext;

public class SpringHolder
{
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext)
    {
        SpringHolder.applicationContext = applicationContext;
    }
}
