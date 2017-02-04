package com.vmantek.chimera.components;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="q2")
@Component
public class Q2Properties
{
    private boolean enabled=true;
    private String defaultArguments="-r";

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getDefaultArguments()
    {
        return defaultArguments;
    }

    public void setDefaultArguments(String defaultArguments)
    {
        this.defaultArguments = defaultArguments;
    }
}
