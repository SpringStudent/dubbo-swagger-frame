1. <u>~该项目还未上传到maven repository,可以下载代码安装到本地体验有部分Bug后面会修复~</u>
   1.必须手动删除ApiServiceScanner生成的class
   2.创建controller的导入包部分代码优化
   3.Controller不支持多个@RequestBody参数，如果dubbo api服务提供者的方法入参有两个或者以上的
   复杂入参，生成的controller到swagger上无法测试；后续通过合并参数解决
   4.通过注解开启dubbo-swagger

**添加maven坐标**

```xml
<dependency>
    <groupId>io.github.springstudent</groupId>
    <artifactId>dubbo-swagger</artifactId>
    <version>0.0.1</version>
</dependency>    
```

**配置扫描DubboApi的扫描器**

```xml
<bean id="apiServiceScanner" class="io.github.springstudent.core.ApiServiceScanner">    
    <property name="classPackage" value="com.gysoft.file.file.controller"/></bean>
```