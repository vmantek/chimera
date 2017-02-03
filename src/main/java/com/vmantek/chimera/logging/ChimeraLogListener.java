package com.vmantek.chimera.logging;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.LogEvent;
import org.jpos.util.LogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ChimeraLogListener implements LogListener, Configurable
{
    String prefix;

    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException
    {
        this.prefix = cfg.get("prefix", null);
    }

    @Override
    public LogEvent log(LogEvent ev)
    {
        final String clsName = ev.getRealm().replace('/', ':');
        Logger logger = LoggerFactory.getLogger(prefix == null ? clsName : prefix + "." + clsName);

        if (logger.isInfoEnabled())
        {
            ByteArrayOutputStream w = new ByteArrayOutputStream();
            PrintStream p = new PrintStream(w);
            ev.dump(p, "");
            logger.info("| \n" + (w.toString().trim()));
        }
        return ev;
    }
}
