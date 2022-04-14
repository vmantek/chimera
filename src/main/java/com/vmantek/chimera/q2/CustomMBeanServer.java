package com.vmantek.chimera.q2;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import javax.management.*;
import javax.management.loading.ClassLoaderRepository;
import java.io.ObjectInputStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Set;

import static com.vmantek.chimera.q2.SpringBootstrapInitializer.getApplicationContext;

public class CustomMBeanServer implements MBeanServer
{
    private final MBeanServer delegate;

    public CustomMBeanServer(MBeanServer delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        return delegate.createMBean(className, name);
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        return delegate.createMBean(className, name, loaderName);
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException
    {
        return delegate.createMBean(className, name, params, signature);
    }

    @Override
    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, MBeanException, NotCompliantMBeanException, InstanceNotFoundException
    {
        return delegate.createMBean(className, name, loaderName, params, signature);
    }

    @Override
    public ObjectInstance registerMBean(Object object, ObjectName name) throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
    {
        enrich(object);
        return delegate.registerMBean(object, name);
    }

    @Override
    public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException
    {
        delegate.unregisterMBean(name);
    }

    @Override
    public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException
    {
        return delegate.getObjectInstance(name);
    }

    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
    {
        return delegate.queryMBeans(name, query);
    }

    @Override
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
    {
        return delegate.queryNames(name, query);
    }

    @Override
    public boolean isRegistered(ObjectName name)
    {
        return delegate.isRegistered(name);
    }

    @Override
    public Integer getMBeanCount()
    {
        return delegate.getMBeanCount();
    }

    @Override
    public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
    {
        return delegate.getAttribute(name, attribute);
    }

    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException
    {
        return delegate.getAttributes(name, attributes);
    }

    @Override
    public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        delegate.setAttribute(name, attribute);
    }

    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException
    {
        return delegate.setAttributes(name, attributes);
    }

    @Override
    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException, MBeanException, ReflectionException
    {
        return delegate.invoke(name, operationName, params, signature);
    }

    @Override
    public String getDefaultDomain()
    {
        return delegate.getDefaultDomain();
    }

    @Override
    public String[] getDomains()
    {
        return delegate.getDomains();
    }

    @Override
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException
    {
        delegate.addNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException
    {
        delegate.addNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException
    {
        delegate.removeNotificationListener(name, listener);
    }

    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException
    {
        delegate.removeNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException
    {
        delegate.removeNotificationListener(name, listener);
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException
    {
        delegate.removeNotificationListener(name, listener, filter, handback);
    }

    @Override
    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException
    {
        return delegate.getMBeanInfo(name);
    }

    @Override
    public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException
    {
        return delegate.isInstanceOf(name, className);
    }

    @Override
    public Object instantiate(String className) throws ReflectionException, MBeanException
    {
        return enrich(delegate.instantiate(className));
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException
    {
//        return enrich(delegate.instantiate(className, loaderName));
        try
        {
            return create(className,loaderName);
        }
        catch (ClassNotFoundException e)
        {
            throw new MBeanException(e);
        }
    }

    @Override
    public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException
    {
        return enrich(delegate.instantiate(className, params, signature));
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature) throws ReflectionException, MBeanException, InstanceNotFoundException
    {
        return enrich(delegate.instantiate(className, loaderName, params, signature));
    }

    private Object enrich(Object o)
    {
        String defaultDomain = getDefaultDomain();

        if(o != null && isQ2(defaultDomain))
        {
            getBeanFactory().autowireBean(o);
        }
        return o;
    }

    private AutowireCapableBeanFactory getBeanFactory()
    {
        return getApplicationContext()
            .getAutowireCapableBeanFactory();
    }

    private Object create(String className, ObjectName loaderName) throws ClassNotFoundException, ReflectionException, InstanceNotFoundException, MBeanException
    {
        String defaultDomain = getDefaultDomain();

        if(isQ2(defaultDomain))
        {
            return getBeanFactory().createBean(Class.forName(className));
        }
        return enrich(delegate.instantiate(className, loaderName));
    }

    private boolean isQ2(String defaultDomain)
    {
        return defaultDomain != null && defaultDomain.equals("Q2");
    }

    @Override
    @Deprecated
    public ObjectInputStream deserialize(ObjectName name, byte[] data) throws InstanceNotFoundException, OperationsException
    {
        return delegate.deserialize(name, data);
    }

    @Override
    @Deprecated
    public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException
    {
        return delegate.deserialize(className, data);
    }

    @Override
    @Deprecated
    public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data) throws InstanceNotFoundException, OperationsException, ReflectionException
    {
        return delegate.deserialize(className, loaderName, data);
    }

    @Override
    public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException
    {
        return delegate.getClassLoaderFor(mbeanName);
    }

    @Override
    public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException
    {
        return delegate.getClassLoader(loaderName);
    }

    @Override
    public ClassLoaderRepository getClassLoaderRepository()
    {
        return delegate.getClassLoaderRepository();
    }
}
