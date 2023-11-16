package com.asset.sync;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.asset.**")
@MapperScan({"com.asset.sync.**.mapper"})
@EnableScheduling
public class AssetSyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetSyncApplication.class, args);
    }

}
