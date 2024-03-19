package io.github.springstudent;

import io.github.springstudent.core.EnableDubboSwagger;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.oas.annotations.EnableOpenApi;


@SpringBootApplication
@EnableOpenApi
@EnableDubbo
@EnableDubboSwagger(mergeParam = true, classPackage = "io.github.springstudent.web", includeApis = {"HelloApiService", "TestApiService"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
