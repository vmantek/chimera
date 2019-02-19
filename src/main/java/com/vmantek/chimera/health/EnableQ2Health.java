package com.vmantek.chimera.health;

import com.vmantek.chimera.db.JPosDatabaseConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(Q2HealhConfiguration.class)
@Documented
public @interface EnableQ2Health
{
}
