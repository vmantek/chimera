package com.vmantek.chimera.deployment;

import com.vmantek.jpos.deployer.ResourceDeployer;
import com.vmantek.jpos.deployer.spi.PropertyResolver;
import com.vmantek.jpos.deployer.springboot.SpringPropertyResolver;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class SysDeployer implements ApplicationContextAware
{
    private ResourceDeployer deployer;
    private String baseDir;
    private ApplicationContext applicationContext;

    public String getBaseDir()
    {
        return baseDir;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    private boolean deleteRecursive(File path) throws FileNotFoundException
    {
        if (!path.exists())
        {
            throw new FileNotFoundException(path.getAbsolutePath());
        }

        boolean ret = true;
        if (path.isDirectory())
        {
            for (File f : path.listFiles())
            {
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    public void start() throws Exception
    {
        ConfigurableEnvironment ce = (ConfigurableEnvironment) applicationContext.getEnvironment();
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        String jvmName = runtimeBean.getName();
        long pid = Long.valueOf(jvmName.split("@")[0]);

        final File tmpDir = new File(".", ".tmp_" + pid);
        if (tmpDir.exists())
        {
            deleteRecursive(tmpDir);
        }

        //noinspection ResultOfMethodCallIgnored
        tmpDir.mkdir();

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        baseDir = tmpDir.getAbsolutePath();
        PropertyResolver resolver = new SpringPropertyResolver(ce);
        resolver.initialize();

        deployer = ResourceDeployer.newInstance(resolver, tmpDir);
        deployer.installRuntimeResources();
        deployer.startConfigMonitoring();
    }

    public void stop()
    {
        try
        {
            deleteRecursive(new File(baseDir));
        }
        catch (FileNotFoundException ignored)
        {
        }
        deployer.stopConfigMonitoring();
    }
}
