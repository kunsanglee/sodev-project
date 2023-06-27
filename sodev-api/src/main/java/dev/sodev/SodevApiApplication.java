package dev.sodev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(scanBasePackages = "dev.sodev")
public class SodevApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SodevApiApplication.class, args);
    }

}
