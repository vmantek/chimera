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
        NewDB newInstance = (NewDB) new NewDB();
        newInstance.setEntityManager(entityManager);
        return newInstance;
    }
}
