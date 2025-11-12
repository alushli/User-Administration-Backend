package com.example.userAdministrationApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.validation.annotation.Validated;

@SpringBootApplication()
@Validated
@EnableRetry
public class UserAdministrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserAdministrationApplication.class, args);
	}

}
