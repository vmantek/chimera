package com.vmantek.chimera.q2;

import javax.management.MBeanServer;
import javax.management.MBeanServerBuilder;
import javax.management.MBeanServerDelegate;

public class CustomMBeanServerBuilder extends MBeanServerBuilder
{
    @Override
    public MBeanServer newMBeanServer(String defaultDomain, MBeanServer outer, MBeanServerDelegate delegate)
    {
        MBeanServer server = super.newMBeanServer(defaultDomain, outer, delegate);
        return new CustomMBeanServer(server);
    }
}
