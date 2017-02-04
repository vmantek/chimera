package com.vmantek.chimera.q2;

import org.springframework.context.ApplicationContext;

public class SpringHolder
{
    private static ApplicationContext applicationContext;
    private static String[] args;

    public static void setArgs(String[] args)
    {
        SpringHolder.args = args;
    }

    public static String[] getArgs()
    {
        return args;
    }

    public static ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext)
    {
        SpringHolder.applicationContext = applicationContext;
    }
}
