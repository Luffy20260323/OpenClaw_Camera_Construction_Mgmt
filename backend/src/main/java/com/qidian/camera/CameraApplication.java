package com.qidian.camera;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 摄像头生命周期管理系统启动类
 * 
 * @author qidian
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.qidian.camera.**.mapper")
public class CameraApplication {

    public static void main(String[] args) {
        SpringApplication.run(CameraApplication.class, args);
        System.out.println("========================================");
        System.out.println("视频监控点位施工项目管理系统启动成功！");
        System.out.println("版本：v1.0.0");
        System.out.println("API 文档：http://localhost:8080/doc.html");
        System.out.println("========================================");
    }

}
