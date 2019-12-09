**实现原理**

使用javassist生成dubbo服务的controller注册到容器，然后这些服务生成的controller就可以被swagger扫描

**添加maven坐标**

```xml
<dependency>
    <groupId>io.github.springstudent</groupId>
    <artifactId>dubbo-swagger</artifactId>
    <version>0.0.6</version>
</dependency>    
```

**注解开启dubbo-swagger**

```java
@EnableDubboSwagger(classPackage = "com.gysoft.file.file.controller")
```

```java
@Target(TYPE)
@Retention(RUNTIME)
@Import(DubboSwaggerRegistar.class)
public @interface EnableDubboSwagger {
    /**
     * 用于配置javassist生成的字节码存储地址，
     * 该包位置必须是已存在的包且能够被<context:component-scan base-package="xxx"/>扫描到
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
     * 生成的controller会有多个@RequestBody，SpringMVC不支持两个或者以上的@RequestBody传参，
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
```

**dubbo-swagger-demo**

https://github.com/SpringStudent/dubbo-swagger-demo

**Q&A**

**1.启动慢**

当dubbo api比较多时项目可能会启动比较慢,这是由于需要javassist创建的class太多导致的，如果有必要的话通过配置@EnableDubboSwagger的includeApis配置，只创建指定的dubbo api的controller来提升项目启动速度

**2.项目启动失败后，重新启动报错如下**

```java
javassist.CannotCompileException: by java.lang.LinkageError: loader (instance of  sun/misc/Launcher$AppClassLoader): attempted  duplicate class definition for name: "io/github/springstudent/web/HelloApiService$DubboController"
...
at io.github.springstudent.core.ApiServiceScanner.writerCompiler(ApiServiceScanner.java:274)...	 
Caused by: java.lang.LinkageError: loader (instance of  sun/misc/Launcher$AppClassLoader): attempted  duplicate class definition for name: "io/github/springstudent/web/HelloApiService$DubboController"
    
```

出现这种情况是由于dubbo-swagger启动中通过javassist创建的class文件由于第一次项目启动失败未及时删除掉，第二次启动会加载到第一次启动dubbo-swagger创建的class，第二次启动也会创建相同的class文件并加载导致。可以删除掉@EnableDubboSwagger的classPackage配置的包下的由dubbo-swagger创建的class重启即可。