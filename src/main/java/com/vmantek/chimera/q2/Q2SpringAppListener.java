package com.vmantek.chimera.q2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;

public class Q2SpringAppListener implements SpringApplicationRunListener
{
    private final SpringApplication application;

    public Q2SpringAppListener(SpringApplication application, String[] args) throws IOException
    {
        this.application = application;
    }

    @Override
    public void started()
    {
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment)
    {

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context)
    {
        SpringHolder.setApplicationContext(context);
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context)
    {

    }

    @Override
    public void finished(ConfigurableApplicationContext context, Throwable exception)
    {

    }
}
