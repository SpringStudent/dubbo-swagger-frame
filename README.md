**实现原理**
使用javassist生成dubbo服务的controller注册到容器，然后这些服务生成的controller就可以被swagger扫描

**添加maven坐标**

```xml
<dependency>
    <groupId>io.github.springstudent</groupId>
    <artifactId>dubbo-swagger</artifactId>
    <version>0.0.1</version>
</dependency>    
```

**方式一:Xml配置扫描DubboApi的扫描器**

```xml
<bean id="apiServiceScanner" class="io.github.springstudent.core.ApiServiceScanner">    
    <!--扫描器生成类的存放包，根据项目调整,该目录必须能够被spring扫描到-->
    <property name="classPackage" value="com.gysoft.file.file.controller"/></bean>
```

**方式二:代码配置扫描DubboApi的扫描器**

```java
@Configuration
public class DubboSwaggerConfig {

    @Bean
    public ApiServiceScanner apiServiceScanner(){
        ApiServiceScanner apiServiceScanner = new ApiServiceScanner();
        apiServiceScanner.setClassPackage("ning.zhou.study.springboot.studyspringboot.web");
        return apiServiceScanner;
    }
}

```

TODO LIST:

1.必须手动删除ApiServiceScanner生成的class

2.创建controller的导入包部分代码优化

3.Controller不支持多个@RequestBody参数，如果dubbo api服务提供者的方法入参有两个或者以上的
   复杂入参，生成的controller到swagger上无法测试；后续通过合并参数解决

4.通过注解开启dubbo-swagger