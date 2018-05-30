package com.zsy.alg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
@MapperScan("com.zsy.alg.mapper")
@EnableDiscoveryClient
public class AlgApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlgApplication.class, args);
	}
}
