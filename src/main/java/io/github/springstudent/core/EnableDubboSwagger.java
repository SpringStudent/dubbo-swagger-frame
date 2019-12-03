package io.github.springstudent.core;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author 周宁
 */
@Target(TYPE)
@Retention(RUNTIME)
@Import(DubboSwaggerRegistar.class)
public @interface EnableDubboSwagger {

    String classPackage();
}
