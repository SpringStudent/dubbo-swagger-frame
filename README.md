~该项目还未上传到maven repository,可以下载代码安装到本地体验有部分Bug后面会修复~
1.必须手动删除ApiServiceScanner生成的class
2.创建controller的导入包部分代码优化

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