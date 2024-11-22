package io.autoflow.app;

import org.dromara.dynamictp.core.spring.EnableDynamicTp;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yiuman
 * @date 2023/7/25
 */
@SpringBootApplication
@EnableFileStorage
@EnableDynamicTp
@MapperScan(basePackages = "io.autoflow.app.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
