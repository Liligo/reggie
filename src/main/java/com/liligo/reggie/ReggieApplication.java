package com.liligo.reggie;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
@MapperScan("com.liligo.reggie.mapper")
public class ReggieApplication {

	public static void main(String[] args) {

		SpringApplication.run(ReggieApplication.class, args);
	}

}
