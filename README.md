敬请期待~该项目还未上传到maven repository,可以下载代码安装到本地体验有部分Bug后面会修复~

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