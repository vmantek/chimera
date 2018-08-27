package com.vmantek.jpos.deployer.support;

import com.vmantek.jpos.deployer.spi.PropertyResolver;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PropertyModel extends StringModel implements TemplateMethodModelEx
{
    private Set<String> keys = new HashSet<>();

    public PropertyModel(PropertyResolver resolver, BeansWrapper wrapper)
    {
        super(resolver, wrapper);
    }

    public Set<String> getKeys()
    {
        return keys;
    }

    protected TemplateModel invokeGenericGet(Map keyMap,
                                             Class clazz,
                                             String key) throws TemplateModelException
    {
        PropertyResolver resolver = (PropertyResolver) object;
        String val = resolver.getProperty(key);
        if (val == null)
        {
            return null;
        }
        keys.add(key);
        return wrap(val);
    }

    public Object exec(List arguments) throws TemplateModelException
    {
        Object key = unwrap((TemplateModel) arguments.get(0));
        return wrap(((PropertyResolver) object).getProperty(key.toString()));
    }
}
