package dev.sodev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dev.sodev")
class SodevCommonApplicationTests {

    public static void main(String[] args) {
        SpringApplication.run(SodevCommonApplicationTests.class, args);
    }

}
