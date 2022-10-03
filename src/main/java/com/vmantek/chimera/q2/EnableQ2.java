package com.vmantek.chimera.q2;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(Q2Configuration.class)
@Documented
@Inherited
public @interface EnableQ2 {
    String deployDirectory() default "deploy";
    boolean enableDeployer() default true;
}
