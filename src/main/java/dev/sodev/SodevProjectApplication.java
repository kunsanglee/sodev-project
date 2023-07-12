package dev.sodev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SodevProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SodevProjectApplication.class, args);
    }

}
