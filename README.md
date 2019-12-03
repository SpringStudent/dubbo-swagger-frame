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

**一个注解开启dubbo-swagger**
classPackage用于配置创建的controller存放目录,该目录一定要能够被spring自动扫描包配置如:"<context:component-scan base-package="xx.xx""包含
```java
@EnableDubboSwagger(classPackage = "ning.zhou.study.springboot.studyspringboot.web")
```

TODO LIST:

1.必须手动删除ApiServiceScanner生成的class

2.创建controller的导入包部分代码优化

3.Controller不支持多个@RequestBody参数，如果dubbo api服务提供者的方法入参有两个或者以上的
   复杂入参，生成的controller到swagger上无法测试；后续通过合并参数解决

4.通过注解开启dubbo-swagger