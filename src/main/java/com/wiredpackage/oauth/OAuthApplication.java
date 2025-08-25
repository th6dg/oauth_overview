package com.wiredpackage.oauth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import org.opencv.core.Core;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.wiredpackage.oauth", "com.wiredpackage.shared", "com.wiredpackage.auth"})
@EnableTransactionManagement
@EnableAsync
public class OAuthApplication {
    public static void main(String[] args) {

        SpringApplication.run(OAuthApplication.class, args);
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        log.info("Hello");
        log.info(String.valueOf(memoryBean.getHeapMemoryUsage().getMax()));
        System.out.println("Loaded: " + Core.NATIVE_LIBRARY_NAME);
        System.out.println("Version: " + Core.VERSION);
    }
}
