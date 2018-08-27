package com.vmantek.chimera.q2;

import com.vmantek.chimera.db.JPosDatabaseAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(Q2Configuration.class)
@Documented
public @interface EnableQ2 {
}
