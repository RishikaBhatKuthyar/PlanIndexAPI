package com.advancedBigDataIndexing.PlanService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRedisRepositories
@EnableRabbit
public class PlanServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(PlanServiceApplication.class, args);
	}
}
