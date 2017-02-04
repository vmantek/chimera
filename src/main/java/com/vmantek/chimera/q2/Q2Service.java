package com.vmantek.chimera.q2;

import com.vmantek.chimera.components.Q2Properties;
import org.jpos.q2.Q2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Q2Service
{
    private static final Logger log = LoggerFactory.getLogger(Q2Service.class);

    private final Q2Properties properties;
    private final String baseDir;

    private Q2 q2;

    public Q2Service(String baseDir, Q2Properties properties)
    {
        this.baseDir = baseDir;
        this.properties = properties;
    }

    private String[] fixupArgs()
    {
        String[] args = SpringHolder.getArgs();
        List<String> _outArgs = new ArrayList<>(16);
        List<String> _inArgs = Arrays.asList(args);
        if (_inArgs.size() > 0 && _inArgs.get(0).equals("q2"))
        {
            Iterator<String> it = _inArgs.iterator();
            it.next();
            while (it.hasNext())
            {
                String s = it.next();
                if ((s.equals("-d") || s.equals("--deploydir")) && it.hasNext())
                {
                    it.next();
                }
                _outArgs.add(s);
            }
        }
        else
        {
            Collections.addAll(_outArgs, properties.getDefaultArguments().split(" "));
        }
        _outArgs.add("-d");
        _outArgs.add(new File(baseDir, "deploy").getAbsolutePath());
        return _outArgs.toArray(new String[_outArgs.size()]);
    }

    public void start() throws Exception
    {
        String[] args = fixupArgs();

        try
        {
            q2 = new Q2(args);
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
