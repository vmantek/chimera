package com.vmantek.chimera.db;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.hibernate.Session;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.jpos.transaction.Context;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManager;
import javax.persistence.spi.PersistenceUnitInfo;
import java.util.Arrays;

public class HibernateUtil
{
    private static final String[] validMethods = new String[]{
        "hashCode",
        "toString",
        "getLog"
    };

    public static Metadata getMetadata(LocalContainerEntityManagerFactoryBean emf)
    {
        StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder();
        ssrb.applySettings(emf.getJpaVendorAdapter().getJpaPropertyMap());
        PersistenceUnitInfo pui = emf.getPersistenceUnitInfo();
        MetadataSources mds = new MetadataSources(ssrb.build());
        for (String s : pui.getManagedClassNames())
        {
            mds.addAnnotatedClassName(s);
        }
        for (String s : pui.getMappingFileNames())
        {
            mds.addResource(s);
        }
        return mds.buildMetadata();
    }

    public static org.jpos.ee.DB getDB(EntityManager entityManager, Context ctx)
    {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(org.jpos.ee.DB.class);
        Class aClass = factory.createClass();
        final org.jpos.ee.DB newInstance;
        try
        {
            newInstance = (org.jpos.ee.DB) aClass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new IllegalStateException("Could not create proxy", e);
        }

        MethodHandler methodHandler = (self, overridden, proceed, args) ->
        {
            if (overridden.getName().equals("session"))
            {
                return entityManager.unwrap(Session.class);
            }
            if (overridden.getName().equals("save"))
            {
                entityManager.persist(args[0]);
                return null;
            }
            if (overridden.getName().equals("saveOrUpdate"))
            {
                return entityManager.merge(args[0]);
            }
            if (overridden.getName().equals("delete"))
            {
                entityManager.remove(args[0]);
                return null;
            }
            else if (Arrays.asList(validMethods).contains(overridden.getName()))
            {
                return proceed.invoke(newInstance, args);
            }
            else
            {
                throw new IllegalStateException(
                    "Method " + overridden.getName() + " cannot be accessed from Transaction " +
                    "Participant");
            }
        };

        ((ProxyObject) newInstance).setHandler(methodHandler);
        return newInstance;
    }

}
