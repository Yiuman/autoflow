package io.autoflow.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yiuman
 * @date 2023/7/25
 */
@SpringBootApplication

public class Application {

    public static void main(String[] args) {
        System.setProperty("proxyHost", "127.0.0.1");
        System.setProperty("proxyPort", "7890");
        SpringApplication.run(Application.class, args);
    }
}
