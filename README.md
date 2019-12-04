**实现原理**

使用javassist生成dubbo服务的controller注册到容器，然后这些服务生成的controller就可以被swagger扫描

**添加maven坐标**

```xml
<dependency>
    <groupId>io.github.springstudent</groupId>
    <artifactId>dubbo-swagger</artifactId>
    <version>0.0.2</version>
</dependency>    
```

**注解开启dubbo-swagger**

```java
@EnableDubboSwagger(classPackage = "com.gysoft.file.file.controller",requestPathPrefix = "pass",mergeParam = true)
```

`classPackage`用于配置javassist生成的字节码存储地址，该包位置必须存在且能够被`<context:component-scan base-package="xxx"/>`扫描到

`requestPathPrefix`请求服务地址的前缀配置，比如你有一个dubbo service的方法名称为`pageQuery`，`requestPathPrefix`配置为`pass`,那么在请求该方法时的path会变成`/pass/pageQuery`

`mergeParam`开启合并参数,为什么要开启合并参数，dubbo api的参数比较复杂时，生成的controller会有多个`@RequestBody`，SpringMVC并不支持两个或者以上的`@RequestBody`传参，在mergeParam(默认开启)开启的场景下会将多个@RequestBody参数合并成一个。

**dubbo-swagger-demo**

https://github.com/SpringStudent/dubbo-swagger-demo
