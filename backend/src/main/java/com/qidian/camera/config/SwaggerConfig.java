package com.qidian.camera.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API 文档配置类
 * 
 * @author qidian
 * @since 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("视频监控点位施工项目管理系统 API")
                        .description("摄像头从规划、施工到验收的全生命周期管理平台")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("北京其点技术服务有限公司")
                                .email("support@qidian.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

}
