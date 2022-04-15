package com.vmantek.chimera.db;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jpos.q2.install.ModuleUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
@EnableTransactionManagement
public class JPosDatabaseConfiguration implements BeanFactoryAware
{
    public static final String MODULES_PKGNAME = "META-INF/org/jpos/ee/modules/";

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        DataSource dataSource,
        HibernateProperties hibernateProperties,
        JpaProperties jpaProperties,
        JpaVendorAdapter jpaVendorAdapter
    )
    {
        Set<String> mappingResources = new HashSet<>();
        Set<String> mappingClasses = new HashSet<>();

        List<String> moduleConfigs = null;
        try
        {
            moduleConfigs = ModuleUtils.getModuleEntries(MODULES_PKGNAME);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Problem fetching Hibernate jPOS modules", e);
        }

        for (String moduleConfig : moduleConfigs)
        {
            SAXReader reader = new SAXReader();

            final URL url = getClass().getClassLoader().getResource(moduleConfig);
            assert url != null;
            final Document doc;
            try
            {
                doc = reader.read(url);
            }
            catch (DocumentException e)
            {
                throw new IllegalStateException("Could not initalize module reader", e);
            }

            Element module = doc.getRootElement().element("mappings");
            if (module == null)
            {
                throw new IllegalStateException("No 'mappings' element in module=" + moduleConfig);
            }

            for (Iterator<Element> l = module.elementIterator("mapping"); l.hasNext(); )
            {
                Element mapping = l.next();
                final String resource = mapping.attributeValue("resource");
                final String clazz = mapping.attributeValue("class");

                if (resource != null)
                {
                    mappingResources.add(resource);
                }
                else if (clazz != null)
                {
                    String p= null;
                    try
                    {
                        p = Class.forName(clazz).getPackage().getName();
                        mappingClasses.add(p);
                    }
                    catch (ClassNotFoundException e)
                    {
                        throw new RuntimeException("Could not load class",e);
                    }
                }
                else
                {
                    throw new IllegalStateException("<mapping> element in configuration specifies no known " +
                                                    "attributes at module " + moduleConfig);
                }
            }
        }

        List<String> mr = jpaProperties.getMappingResources();
        String[] mra= (!ObjectUtils.isEmpty(mr)
                ? StringUtils.toStringArray(mr) : new String[]{});

        LocalContainerEntityManagerFactoryBean em
            = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPersistenceUnitName("default");
        em.setJpaVendorAdapter(jpaVendorAdapter);
        final Map<String, Object> props = hibernateProperties
            .determineHibernateProperties(
                jpaProperties.getProperties(),
                new HibernateSettings());
        em.getJpaPropertyMap().putAll(props);
        em.getJpaPropertyMap().putAll(jpaProperties.getProperties());

        Set<String> pkgs = new LinkedHashSet<>();
        pkgs.addAll(mappingClasses);
        pkgs.addAll(Arrays.asList(getPackagesToScan()));
        Set<String> mrSet = new LinkedHashSet<>();
        mrSet.addAll(Arrays.asList(mra));
        mrSet.addAll(mappingResources);

        em.setPackagesToScan(StringUtils.toStringArray(pkgs));
        em.setMappingResources(StringUtils.toStringArray(mrSet));
        return em;
    }

    private String[] getPackagesToScan()
    {
        List<String> packages = EntityScanPackages.get(this.beanFactory)
            .getPackageNames();
        if (packages.isEmpty() && AutoConfigurationPackages.has(this.beanFactory))
        {
            packages = AutoConfigurationPackages.get(this.beanFactory);
        }
        return StringUtils.toStringArray(packages);
    }
}
