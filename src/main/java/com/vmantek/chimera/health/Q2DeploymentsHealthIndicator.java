package com.vmantek.chimera.health;

import org.jpos.q2.QBean;
import org.jpos.q2.iso.QMUXMBean;
import org.jpos.transaction.TransactionManagerMBean;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.util.StringUtils;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Q2DeploymentsHealthIndicator extends AbstractHealthIndicator
{
    private MBeanServer getMBeanServer()
    {
        MBeanServer server;
        ArrayList mbeanServerList =
            MBeanServerFactory.findMBeanServer(null);
        if (mbeanServerList.isEmpty())
        {
            server = null;
        }
        else
        {
            server = (MBeanServer) mbeanServerList.get(0);
        }
        return server;
    }

    @Override
    protected void doHealthCheck(Builder builder) throws Exception
    {
        final MBeanServer server = getMBeanServer();

        if (server != null)
        {
            final ObjectName on = new ObjectName("Q2:type=qbean,service=*");
            final Set<ObjectName> names = server.queryNames(on, null);
            final Stream<ObjectName> s = names.stream();

            final long muxInTransitCount = getMuxInTransitCount(server, names);
            final long tmInTransitCount = getTmInTransitCount(server, names);

            builder.withDetail("mux.in-transit", String.valueOf(muxInTransitCount));
            builder.withDetail("tm.in-transit", String.valueOf(tmInTransitCount));
            builder.withDetail("in-transit", String.valueOf(Math.max(muxInTransitCount, tmInTransitCount)));

            if (s.allMatch(name -> getStatus(server, name) == QBean.STARTED))
            {
                builder.up();
            }
            else if (s.anyMatch(name -> getStatus(server, name) == QBean.FAILED))
            {
                long cnt = s.filter(name -> getStatus(server, name) == QBean.FAILED).count();

                Set<String> objs = s.filter(name -> getStatus(server, name) == QBean.FAILED)
                    .map(ObjectName::getCanonicalName)
                    .collect(Collectors.toSet());

                String failedServices = StringUtils.collectionToCommaDelimitedString(objs);
                builder.withDetail("failed-count", String.valueOf(cnt));
                builder.withDetail("failed-services", failedServices);
                builder.status("failed");
            }
            else if (s.allMatch(name -> getStatus(server, name) == QBean.DESTROYED))
            {
                builder.outOfService();
            }
            else if (s.allMatch(name -> getStatus(server, name) == QBean.STOPPED))
            {
                builder.down();
            }
            else
            {
                builder.unknown();
            }
        }
        else
        {
            builder.unknown();
        }
    }

    private int getStatus(MBeanServer server, ObjectName name)
    {
        try
        {
            return (Integer) server.getAttribute(name, "State");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    private long getMuxInTransitCount(MBeanServer server, Set<ObjectName> names) throws Exception
    {
        final String clsName = QMUXMBean.class.getName();

        long cnt = 0;
        for (ObjectName name : names)
        {
            if (server.isInstanceOf(name, clsName))
            {
                Map<String, String> counters = parseMuxCounters((String) server.getAttribute(name, "CountersAsString"));
                if (counters.get("connected").equals("true"))
                {
                    cnt += Long.valueOf(counters.get("rx_pending"));
                }
            }
        }
        return cnt;
    }

    private long getTmInTransitCount(MBeanServer server, Set<ObjectName> names) throws Exception
    {
        final String clsName = TransactionManagerMBean.class.getName();

        long cnt = 0;
        for (ObjectName name : names)
        {
            if (server.isInstanceOf(name, clsName))
            {
                cnt += (Integer) server.getAttribute(name, "OutstandingTransactions");
            }
        }
        return cnt;
    }

    private Map<String, String> parseMuxCounters(String counters)
    {
        Map<String, String> res = new HashMap<>();
        for (String section : counters.split(","))
        {
            section = section.trim();
            String[] segs = section.split("=");
            if (segs.length == 2)
            {
                res.put(segs[0].trim(), segs[1].trim());
            }
        }
        return res;
    }
}
