package com.vmantek.chimera.q2;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;

import java.security.ProtectionDomain;

public class Q2Mods
{
    private static boolean modsApplied = false;

    public synchronized static void patchQ2()
    {
        if(modsApplied) return;

        final ClassLoader cl = SpringHolder.class.getClassLoader();
        final ProtectionDomain pd = SpringHolder.class.getProtectionDomain();
        final ClassPool cp = ClassPool.getDefault();
        final ClassLoader appCL = Q2Mods.class.getClassLoader();
        cp.appendClassPath(new LoaderClassPath(appCL));
        cp.importPackage("com.vmantek.chimera.q2.SpringHolder");

        // Prevent adding shutdown hook
        String q2ClassName = "org.jpos.q2.Q2";

        try
        {
            CtClass clz = cp.get(q2ClassName);
            clz.getDeclaredMethod("addShutdownHook").setBody("return;");
            clz.toClass(cl, pd);
            clz.detach();
        }
        catch (NotFoundException | CannotCompileException e)
        {
            throw new IllegalStateException("Could not patch Q2", e);
        }

        // Use Session Factory from Spring JPA
        try
        {
            CtClass clz = cp.get("org.jpos.ee.DB");
            cp.get("org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean");

            CtMethod mm;

            try
            {
                mm = clz.getDeclaredMethod("newSessionFactory");
            }
            catch (NotFoundException e)
            {
                mm = clz.getDeclaredMethod("getSessionFactory");
            }

            String abody = "{ javax.persistence.EntityManagerFactory emf=" +
                           "(javax.persistence.EntityManagerFactory)SpringHolder.getApplicationContext()" +
                           ".getBean(javax.persistence.EntityManagerFactory.class);" +
                           "return emf.unwrap(org.hibernate.SessionFactory.class); }";
            mm.setBody(abody);

            mm = clz.getDeclaredMethod("getMetadata");
            abody = "{ " +
                    "org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean emf=" +
                    "(org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean)SpringHolder.getApplicationContext()" +
                    ".getBean(org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean.class);" +
                    "return com.vmantek.chimera.db.HibernateUtil.getMetadata(emf); }";
            mm.setBody(abody);

            clz.toClass(cl, pd);
            clz.detach();
        }
        catch (NotFoundException ignored)
        {
        }
        catch (CannotCompileException e)
        {
            throw new IllegalStateException("Could not patch Q2", e);
        }

        // Make QBeans and any Q2 Instantiated bean be autowirable
        try
        {
            CtClass clz = cp.get("org.jpos.q2.QFactory");

            String abody = "SpringHolder.getApplicationContext()" +
                    ".getAutowireCapableBeanFactory()" +
                    ".autowireBean($_);";

            CtMethod mm = clz.getDeclaredMethod("newInstance",
                                                new CtClass[]{cp.get("java.lang.String")});
            mm.insertAfter(abody);

            abody = "SpringHolder.getApplicationContext()" +
                    ".getAutowireCapableBeanFactory()" +
                    ".autowireBean(obj);";

            mm = clz.getDeclaredMethod("createQBean",
                                       new CtClass[]{cp.get(q2ClassName),
                                                     cp.get("org.jdom2.Element"),
                                                     cp.get("java.lang.Object")});
            mm.insertBefore(abody);
            clz.toClass(cl, pd);
            clz.detach();
            modsApplied = true;
        }
        catch (NotFoundException | CannotCompileException e)
        {
            throw new IllegalStateException("Could not patch Q2", e);
        }
    }
}
