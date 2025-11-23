package com.cotato.itda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// DB 세팅되면 exclude 옵션 제거하기
@SpringBootApplication(excludeName = {"org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"})
public class ItdaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItdaApplication.class, args);
	}

}
