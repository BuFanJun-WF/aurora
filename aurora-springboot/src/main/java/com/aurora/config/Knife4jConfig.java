package com.aurora.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.Collections;

/**
 * @EnableSwagger2WebMvc
 * 注解来启用Knife4j的Swagger文档生成功能。
 */
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    /**
     * 用于配置Swagger文档的生成规则
     * @return 返回Docket对象
     */
    @Bean
    public Docket createRestApi() {
        // 表示使用Swagger 2规范
        return new Docket(DocumentationType.SWAGGER_2)
                // 设置接口使用的协议
                .protocols(Collections.singleton("http"))
                // 设置接口的主机地址
                .host("http://175.178.130.142/")
                // 配置API文档的基本信息
                .apiInfo(apiInfo())
                .select()
                // 用于指定扫描的控制器包路径
                .apis(RequestHandlerSelectors.basePackage("com.aurora.controller"))
                // 配置扫描的路径规则为所有路径
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("aurora文档")
                .description("aurora")
                .contact(new Contact("布凡君", "", "1730705091@qq.com"))
                .termsOfServiceUrl("http://175.178.130.142/api")
                .version("1.0")
                .build();
    }

}
