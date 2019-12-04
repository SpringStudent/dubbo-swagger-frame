package io.github.springstudent.core;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * @author 周宁
 */
public class DubboSwaggerRegistar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableDubboSwagger.class.getName()));
        String classPackage = attributes.getString("classPackage");
        String requestPathPrefix = attributes.getString("requestPathPrefix");
        boolean mergeParam = attributes.getBoolean("mergeParam");
        BeanDefinitionBuilder builder = rootBeanDefinition(ApiServiceScanner.class);
        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        builder.addPropertyValue("classPackage", classPackage);
        builder.addPropertyValue("requestPathPrefix", requestPathPrefix);
        builder.addPropertyValue("mergeParam", mergeParam);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        BeanDefinitionReaderUtils.registerWithGeneratedName(beanDefinition, registry);
    }

}
