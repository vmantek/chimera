package com.vmantek.chimera.db;

import com.vmantek.chimera.q2.Q2Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(JPosDatabaseConfiguration.class)
@Documented
public @interface EnableQ2DatabaseIntegration
{
}
