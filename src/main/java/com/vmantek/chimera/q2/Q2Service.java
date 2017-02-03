package com.vmantek.chimera.q2;

import org.jpos.q2.Q2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class Q2Service
{
    public static final String Q2_SIGNAL_NAME = "_Q2Service";
    private static final Logger log = LoggerFactory.getLogger(Q2Service.class);
    Q2 q2;
    String baseDir;

    public Q2Service(String baseDir)
    {
        this.baseDir = baseDir;
    }

    public Q2 getQ2()
    {
        return q2;
    }

    public void start() throws Exception
    {
        try
        {
            String[] xargs = new String[]{"-r", "-d", new File(baseDir, "deploy").getAbsolutePath()};
            q2 = new Q2(xargs);
            q2.start();
            log.info("Started Q2 Service");
        }
        catch (Exception e)
        {
            log.error("Could not start Q2 service", e);
            throw e;
        }
    }

    public void stop() throws Exception
    {
        try
        {
            if (q2 != null)
            {
                q2.shutdown(true);
                log.info("Shutdown Q2 Service");
            }
        }
        catch (Exception ignored)
        {
        }
        finally
        {
            q2 = null;
        }
    }
}
