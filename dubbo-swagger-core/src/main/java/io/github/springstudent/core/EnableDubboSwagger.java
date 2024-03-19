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
    /**
     * 用于配置javassist生成的字节码存储地址
     * 该包位置必须是已存在的包且能够被context:component-scan base-package="xxx"扫描到
     * 配置示例:com.gysoft.file.file.controller
     * @return String
     */
    String classPackage();

    /**
     * 请求服务地址的前缀配置，比如你有一个dubbo service的方法名称为pageQuery,
     * requestPathPrefix配置为pass,那么在请求该方法时的path会变成/pass/pageQuery
     * 配置示例:pass
     * @return String
     */
    String requestPathPrefix() default "";

    /**
     * 用于开启合并参数，为什么要开启合并参数？因为：dubbo api的参数比较复杂时，
     * 生成的controller会有多个@RequestBody，SpringMVC并不支持两个或者以上的@RequestBody传参，
     * 在开启合并参数的场景下会将多个@RequestBody参数合并成一个。
     * @return boolean
     */
    boolean mergeParam() default true;

    /**
     * 用于指定生成swagger api的dubbo api服务名称,使用dubbo api的类名称进行配置，
     * 不配置则扫描全部dubbo api服务
     * 配置示例:{"HelloApiService","TestApiService"}
     * @return String[]
     */
    String[] includeApis() default {};
}
