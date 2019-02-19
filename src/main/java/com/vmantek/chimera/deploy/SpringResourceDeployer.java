package com.vmantek.chimera.deploy;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.ByteSource;
import com.vmantek.chimera.deploy.support.AntPathMatcher;
import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.jpos.q2.install.ModuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static freemarker.template.Configuration.VERSION_2_3_23;

@SuppressWarnings({"ResultOfMethodCallIgnored", "SimplifyStreamApiCallChains", "RegExpRedundantEscape", "Duplicates"})
public class SpringResourceDeployer
{
    private static final Logger log = LoggerFactory.getLogger(SpringResourceDeployer.class);
    private static final Pattern pattern1 = Pattern.compile("\\$\\{(.*?)\\}");
    private static final Pattern pattern2 = Pattern.compile("@@(.*?)@@");
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static final ClassLoader cl = SpringResourceDeployer.class.getClassLoader();
    private static SpringResourceDeployer INSTANCE = null;
    private final String deployerName;

    private String resourcePrefix = "META-INF/q2-runtime";
    private List<String> filterExclusions = new ArrayList<>();

    private File outputBase;
    private Multimap<String, String> resourceProps = TreeMultimap.create();

    private ConfigurableEnvironment environment;

    public SpringResourceDeployer(ConfigurableEnvironment environment,String deployerName)
    {
        this.deployerName = deployerName;
        this.environment = environment;
    }

    public String getResourcePrefix()
    {
        return resourcePrefix;
    }

    public void setResourcePrefix(String resourcePrefix)
    {
        this.resourcePrefix = resourcePrefix;
    }

    private boolean deleteRecursive(File path) throws FileNotFoundException
    {
        if (!path.exists())
        {
            throw new FileNotFoundException(path.getAbsolutePath());
        }

        boolean ret = true;
        if (path.isDirectory())
        {
            for (File f : path.listFiles())
            {
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

    public File getOutputBase()
    {
        return outputBase;
    }

    public void start() throws Exception
    {
        final File tmpDir = getBaseOutputPath();
        if (tmpDir.exists())
        {
            deleteRecursive(tmpDir);
        }

        //noinspection ResultOfMethodCallIgnored
        tmpDir.mkdir();

        outputBase = tmpDir.getAbsoluteFile();

        installRuntimeResources();
    }

    protected File getBaseOutputPath()
    {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        String jvmName = runtimeBean.getName();
        long pid = Long.valueOf(jvmName.split("@")[0]);
        return new File(".", ".tmp_" + deployerName + "_"+ pid);
    }

    public void stop()
    {
        try
        {
            deleteRecursive(outputBase);
        }
        catch (FileNotFoundException ignored)
        {
        }
    }

    public void setFilterExclusions(Collection<String> exclusions)
    {
        filterExclusions.clear();
        exclusions.forEach(this::addFilterExclusion);
    }

    public void addFilterExclusion(String pattern)
    {
        if (!antPathMatcher.isPattern(pattern))
        {
            throw new IllegalArgumentException("Invalid pattern: " + pattern);
        }
        filterExclusions.add(pattern);
    }

    public void removeFilterExclusion(String pattern)
    {
        filterExclusions.remove(pattern);
    }

    protected void setupDefaultExclusions()
    {
        filterExclusions.clear();
        filterExclusions.add("cfg/*.ks");
        filterExclusions.add("cfg/*.jks");
        filterExclusions.add("**/*.jpg");
        filterExclusions.add("**/*.gif");
        filterExclusions.add("**/*.png");
        filterExclusions.add("**/*.pdf");
    }

    public List<String> getAvailableResources() throws IOException
    {
        return ModuleUtils.getModuleEntries(resourcePrefix);
    }

    public void installRuntimeResources() throws IOException
    {
        resourceProps.clear();
        List<String> entries = getAvailableResources();
        final List<String> filtered = entries
            .stream()
            .filter(this::isResourceFilterable)
            .collect(Collectors.toList());

        for (String resource : entries)
        {
            installResource(resource, filtered.contains(resource));
        }
    }

    public void installResource(String resource) throws IOException
    {
        resourceProps.clear();
        installResource(resource, isResourceFilterable(resource));
    }

    public void installResource(String resource, boolean filtered) throws IOException
    {
        final URL rez = getResource(resource);
        final String filename = resourceToFilename(resource);
        File outputFile = new File(outputBase, filename);

        final File dir = outputFile.getParentFile();
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        if (!filtered)
        {
            try (FileOutputStream output = new FileOutputStream(outputFile))
            {
                new UrlByteSource(rez).copyTo(output);
            }
        }
        else
        {
            try (final FileWriter w = new FileWriter(outputFile))
            {
                String doc = new UrlByteSource(rez)
                    .asCharSource(Charset.defaultCharset())
                    .read();

                // We first try with ${prop}
                doc = filterResource(resource, pattern1, doc);
                // Then with @@prop@@
                doc = filterResource(resource, pattern2, doc);

                // Ultimately we do FreeMarker processing
                try
                {
                    doc = filterText(resource, doc);
                }
                catch (TemplateException e)
                {
                    log.error("Could not apply template", e);
                }

                // Write the filtered resource
                try
                {
                    w.write(doc);
                    w.flush();
                }
                catch (Throwable e)
                {
                    log.error("Could not write file: " + outputFile.getAbsolutePath(), e);
                }
            }
        }
    }

    public void uninstallResource(String resource) throws IOException
    {
        final String filename = resourceToFilename(resource);
        File outputFile = new File(outputBase, filename);
        if (outputFile.exists())
        {
            outputFile.delete();
        }
    }

    private boolean isResourceFilterable(String resource)
    {
        return !filterExclusions
            .stream()
            .anyMatch(e -> antPathMatcher.match(e, resourceToFilename(resource)));
    }

    private String resourceToFilename(String resource)
    {
        return resource.substring(resourcePrefix.length() + 1);
    }

    private String filterText(String resource, String doc) throws IOException, TemplateException
    {
        BeansWrapper bw = new DefaultObjectWrapperBuilder(VERSION_2_3_23)
            .build();
        PropertyModel mm = new PropertyModel(this, bw);
        StringTemplateLoader loader = new StringTemplateLoader();
        loader.putTemplate(resource, doc, System.currentTimeMillis());
        Configuration c = new Configuration(VERSION_2_3_23);
        c.setTemplateLoader(loader);
        c.setTagSyntax(Configuration.SQUARE_BRACKET_TAG_SYNTAX);
        Template t = c.getTemplate(resource);

        StringWriter sw = new StringWriter();
        t.process(mm, sw);
        return sw.toString();
    }

    private String filterResource(String resource, Pattern pattern, String s) throws IOException
    {
        Matcher m = pattern.matcher(s);
        StringBuffer sb = new StringBuffer(s.length() * 2);

        while (m.find())
        {
            String key = m.group(1);
            String val = getConfigProperty(key);
            if (val != null)
            {
                m.appendReplacement(sb, val);
            }
        }

        m.appendTail(sb);
        return sb.toString();
    }

    private String getConfigProperty(String key)
    {
        Iterable<ConfigurationPropertySource> sources
            = ConfigurationPropertySources.get(environment);

        Object value=null;
        for (ConfigurationPropertySource source : sources)
        {
            ConfigurationPropertyName pname = null;
            try
            {
                pname = ConfigurationPropertyName.of(key);
            }
            catch (Exception e)
            {
                return null;
            }
            ConfigurationProperty prop = source.getConfigurationProperty(
                pname);
            if(prop!=null)
            {
                value=prop.getValue();
                break;
            }
        }
        return value==null?null:value.toString();
    }

    private URL getResource(String resourceName)
    {
        ClassLoader loader = MoreObjects.firstNonNull(
            Thread.currentThread().getContextClassLoader(),
            SpringResourceDeployer.class.getClassLoader());
        return loader.getResource(resourceName);
    }

    private static final class UrlByteSource extends ByteSource
    {
        private final URL url;

        private UrlByteSource(URL url)
        {
            this.url = checkNotNull(url);
        }

        @Override
        public InputStream openStream() throws IOException
        {
            return url.openStream();
        }
    }

    private class PropertyModel extends StringModel implements TemplateMethodModelEx
    {
        private Set<String> keys = new HashSet<>();

        public PropertyModel(SpringResourceDeployer resolver, BeansWrapper wrapper)
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
            SpringResourceDeployer resolver = (SpringResourceDeployer) object;
            String val = resolver.getConfigProperty(key);
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
            return wrap(((SpringResourceDeployer) object).getConfigProperty(key.toString()));
        }
    }
}
