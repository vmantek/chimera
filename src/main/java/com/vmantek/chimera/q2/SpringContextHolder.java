package com.vmantek.chimera.q2;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringContextHolder implements ApplicationContextAware
{
    private static ConfigurableApplicationContext context;

    public static <T extends Object> T createBean(Class<T> cls)
    {
        try
        {
            return context.getBean(cls);
        }
        catch (BeansException e)
        {
            AutowireCapableBeanFactory bf = getBeanFactory();
            return bf.createBean(cls);
        }
    }

    private static AutowireCapableBeanFactory getBeanFactory()
    {
        return context.getAutowireCapableBeanFactory();
    }

    public static void autowireBean(Object o)
    {
        getBeanFactory().autowireBean(o);
    }

    public static void setContext(ApplicationContext ctx)
    {
        SpringContextHolder.context = (ConfigurableApplicationContext) ctx;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException
    {
        SpringContextHolder.context = (ConfigurableApplicationContext) context;
    }
}
