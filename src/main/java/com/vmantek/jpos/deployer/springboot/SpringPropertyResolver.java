package com.vmantek.jpos.deployer.springboot;

import com.vmantek.jpos.deployer.spi.PropertyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpringPropertyResolver implements PropertyResolver
{
    private static final Logger log = LoggerFactory.getLogger(SpringPropertyResolver.class);
    private ConfigurableEnvironment environment;

    public SpringPropertyResolver(ConfigurableEnvironment environment)
    {
        this.environment = environment;
    }

    @Override
    public void initialize() throws IOException
    {
    }

    @Override
    public String getProperty(String key)
    {
        return environment.getProperty(key);
    }
}
