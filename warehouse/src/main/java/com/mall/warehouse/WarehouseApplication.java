package com.mall.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商城系统-仓储系统
 *
 * @author Daoyuan
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaClient
@RestController
public class WarehouseApplication {

    @GetMapping("actuator/info")
    public String info() {
        return "仓储系统";
    }

    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
