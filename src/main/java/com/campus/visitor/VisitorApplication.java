package com.campus.visitor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.campus.visitor.modules.*.mapper")
@EnableAsync
public class VisitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(VisitorApplication.class, args);
	}

}


