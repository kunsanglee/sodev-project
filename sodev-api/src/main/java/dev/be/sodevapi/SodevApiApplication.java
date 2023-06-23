package dev.be.sodevapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = { "dev.be.sodevapi", "dev.be.sodevcommon" }
)
public class SodevApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SodevApiApplication.class, args);
    }

}
