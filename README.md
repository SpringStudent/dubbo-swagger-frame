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
@EnableDubboSwagger(classPackage = "com.gysoft.file.file.controller",requestPathPrefix = "pass")
```

`classPackage`用于配置javassist生成的字节码存储地址，该包位置必须存在且能够被`<context:component-scan base-package="xxx"/>`扫描到

`requestPathPrefix`请求服务地址的前缀配置，比如你有一个dubbo service的方法名称为`pageQuery`，`requestPathPrefix`配置为`pass`,那么在请求该方法时的path会变成`/pass/pageQuery`

##### TODO LIST:

Controller不支持多个@RequestBody参数，如果dubbo api服务提供者的方法入参有两个或者以上的
   复杂入参，生成的controller到swagger上无法测试；后续通过合并参数解决